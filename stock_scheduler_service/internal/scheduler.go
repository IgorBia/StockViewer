package internal

import (
	"database/sql"
	"time"

	log "github.com/sirupsen/logrus"
)

func ScheduleCandleUpdates(db *sql.DB, symbol string, interval string, duration time.Duration) {
	ticker := alignedTicker(duration)

	go func() {
		for {
			select {
			case <-ticker.C:
				Update(db, symbol, interval)
			}
		}
	}()
}

func alignedTicker(duration time.Duration) *time.Ticker {
	now := time.Now()
	next := now.Truncate(duration).Add(duration)
	time.Sleep(time.Until(next.Add(2*time.Second))) // Adding a small buffer to ensure alignment
	return time.NewTicker(duration)
}

func Update(db *sql.DB, symbol string, interval string) {
	log.Infof("Updating %s for %s", interval, symbol)
	candleData := fetchCandleData(FetchConfig{Symbol: symbol, Interval: interval})
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
			log.Infof(err.Error())
		}
	}
}
