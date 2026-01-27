package internal

import (
	"database/sql"
	"time"

	"github.com/gofrs/uuid"
	log "github.com/sirupsen/logrus"
)

func ScheduleCandleUpdates(db *sql.DB, symbol string, interval string, duration time.Duration) {
    if duration <= 0 {
        log.WithFields(log.Fields{"symbol": symbol, "interval": interval}).Warn("Skipping schedule: non-positive duration")
        return
    }

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
    if duration <= 0 {
        panic("alignedTicker called with non-positive duration")
    }
	now := time.Now()
	next := now.Truncate(duration).Add(duration)
	time.Sleep(time.Until(next.Add(2 * time.Second)))
	return time.NewTicker(duration)
}

func Update(db *sql.DB, symbol string, interval string) {
	start := time.Now()
    lg := log.WithFields(log.Fields{"symbol": symbol, "interval": interval})

    lg.Infof("Starting update")

    candleData := fetchCandleData(FetchConfig{
        Symbol:    symbol,
        Interval:  interval,
        Open_Time: nil,
        Limit:     2,
    })
    lg = lg.WithField("fetched", len(candleData))
    lg.Debug("Fetched candle data")

    if len(candleData) == 0 {
        lg.Debug("No candle data to insert")
        lg.WithField("duration_ms", time.Since(start).Milliseconds()).Debug("Update finished (nothing to do)")
        return
    }

	ids, err := insertCandleData(db, candleData, interval, symbol)
	 if err != nil {
        lg.WithError(err).Errorf("Failed to insert candle data for %s %s", symbol, interval)
        lg.WithField("duration_ms", time.Since(start).Milliseconds()).Error("Update finished with errors")
        return
    }

    if len(ids) == 0 {
        lg.Info("No new candles were inserted")
        lg.WithField("duration_ms", time.Since(start).Milliseconds()).Info("Update finished")
        return
    }

    go func(idsToPublish []uuid.UUID) {
        if err := publishCandleEvent([]string{"kafka:9092"}, "indicator_events", idsToPublish); err != nil {
            log.WithFields(log.Fields{"symbol": symbol, "interval": interval}).WithError(err).Warn("Could not publish event for candle(s)")
            for _, id := range idsToPublish {
                log.Infof(" %v ", id)
            }
        }
    }(ids)

    lg.WithField("duration_ms", time.Since(start).Milliseconds()).Info("Update finished (publish async)")
}
