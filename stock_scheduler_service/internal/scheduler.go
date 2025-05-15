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
				err := insertCandleData(db, fetchCandleData(FetchConfig{Symbol: symbol, Interval: interval}), interval, symbol)
				if err != nil {
					log.WithError(err).Errorf("Failed to update %s for %s", interval, symbol)
				} else {
					log.Infof("Updated %s for %s", interval, symbol)
				}
			}
		}
	}()
}
