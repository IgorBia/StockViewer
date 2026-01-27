package internal

import (
	"database/sql"
	log "github.com/sirupsen/logrus"
	"time"
	"github.com/igorbia/stock_scheduler_service/config"
)

func SetupDB(db *sql.DB, appConfig config.AppConfig) {
	log.Info("Checking data pressence")

    for _, symbol := range appConfig.Symbols {
        for _, interval := range appConfig.Intervals {
            d, ok := appConfig.Durations[interval]
            if !ok || d <= 0 {
                log.Warnf("Skipping inject for %s %s: missing or non-positive duration", symbol, interval)
                continue
            }
            go injectDataForSymbolAndInterval(db, symbol, interval, d)
        }
    }
}
func injectDataForSymbolAndInterval(db *sql.DB, symbol string, interval string, duration time.Duration) {
	oldestCandleTime := getOldestCandleOpenTime(duration)
	if !isCandlePresent(db, symbol, interval, oldestCandleTime) {
		candleData := fetchCandleData(FetchConfig{Symbol: symbol, Interval: interval, Open_Time: &oldestCandleTime, Limit: 1})
		log.WithFields(log.Fields{"symbol": symbol, "interval": interval, "count": len(candleData)}).Debug("Update: fetched candles count")
		ids, err := insertCandleData(db, candleData, interval, symbol)
		if err != nil {
			log.WithError(err).Errorf("Failed to update %s for %s", interval, symbol)
		} else {
			log.Infof("Updated %s for %s", interval, symbol)
			err := publishCandleEvent([]string{"kafka:9092"}, "indicator_events", ids)
			if err != nil {
				log.Infof("Could not publish an event for candle/s")
				for i := 0; i < len(ids); i++ {
					log.Infof(" %v ", ids[i])
				}
				log.Info(err.Error())
			}
		}
	}
}

func getOldestCandleOpenTime(duration time.Duration) time.Time {
	return time.Now().Add(-duration * 200)
}

func isCandlePresent(db *sql.DB, symbol string, interval string, oldestCandleTime time.Time) bool {
	var count int
	pairId, err := getPairId(db, symbol)
	query := `SELECT COUNT(*) FROM stock_data.candle WHERE pair_id = $1 AND timeframe = $2 AND open_time = $3`
	err = db.QueryRow(query, pairId, interval, oldestCandleTime).Scan(&count)
	if err != nil {
		log.WithError(err).Error("Failed to check candle presence")
		return false
	}
	return count > 0
}
