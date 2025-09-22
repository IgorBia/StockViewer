package internal

import (
	"database/sql"
	"time"

	log "github.com/sirupsen/logrus"
)

func ScheduleCandleUpdates(db *sql.DB, symbol string, interval string, duration time.Duration) {
	ticker := time.NewTicker(duration)

	go func() {
		for {
			select {
			case <-ticker.C:
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
					}
				}
			}
		}
	}()
}
