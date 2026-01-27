package internal

import (
	"database/sql"
	"github.com/igorbia/stock_scheduler_service/config"
	"fmt"
	"github.com/gofrs/uuid"
	log "github.com/sirupsen/logrus"
	"time"
)

/*
Data Integration verification:

	For every symbol and timeframe:
		1. Get the oldest candle
		2. Get the next candle if not in future
		3. If missing fetch the candle
		4. Repeat step 2 until next candle in future
*/
func EnsureDataIntegrity(db *sql.DB, appConfig config.AppConfig) {
	log.Info("Starting data integrity verification routines for all supported symbols and intervals.")

	for _, symbol := range appConfig.Symbols {
		for _, interval := range appConfig.Intervals {
			d, ok := appConfig.Durations[interval]
			if !ok || d <= 0 {
				log.Warnf("Skipping integrity check for %s %s: missing or non-positive duration", symbol, interval)
				continue
			}
			go ensureDataIntegrityForSymbolAndInterval(db, symbol, interval, d)
		}
	}
}
func ensureDataIntegrityForSymbolAndInterval(db *sql.DB, symbol string, interval string, duration time.Duration) {
	 log.Infof("Starting data integrity verification for %s %s", symbol, interval)

    pairId, err := getPairId(db, symbol)
    if err != nil {
        log.WithFields(log.Fields{"symbol": symbol}).WithError(err).Error("Failed to get pair ID")
        return
    }

	actualCandleOpenTime, err := getTheOldestCandleOpenTime(db, pairId, interval)
    if err != nil {
        log.WithFields(log.Fields{"pairId": pairId, "interval": interval}).WithError(err).Error("Failed to get the oldest candle")
        return
    }

    if actualCandleOpenTime == nil {
        log.WithFields(log.Fields{"pairId": pairId, "interval": interval}).Info("No candles present, skipping integrity check")
        return
    }

    const maxPatchAttempts = 5

    for {
        nextOpenTime, err := calculateNextOpenTime(actualCandleOpenTime, duration)
        if err != nil {
            log.Infof("Data integrity verified for %s %s up to current time: %v", symbol, interval, err)
            return
        }
        nextCandleOpenTime, err := getNextCandleOpenTime(db, pairId, interval, nextOpenTime)
        if err != nil {
            if err != sql.ErrNoRows {
                log.WithFields(log.Fields{"pairId": pairId, "interval": interval}).WithError(err).Error("Error fetching next candle")
                return
            }
            log.WithFields(log.Fields{"pairId": pairId, "interval": interval, "missingOpen": nextOpenTime}).Info("Missing candle - attempting patch")
            var perr error
            for attempt := 1; attempt <= maxPatchAttempts; attempt++ {
                perr = patchCandle(db, nextOpenTime, symbol, interval)
                if perr == nil {
                    actualCandleOpenTime = nextOpenTime
                    time.Sleep(100 * time.Millisecond)
                    break
                }
                log.WithFields(log.Fields{"symbol": symbol, "interval": interval, "open": nextOpenTime, "attempt": attempt}).WithError(perr).Warn("Patch attempt failed")
                time.Sleep(time.Duration(attempt) * 2 * time.Second)
            }
            if perr != nil {
                log.WithFields(log.Fields{"symbol": symbol, "interval": interval}).WithError(perr).Errorf("Failed to patch missing candle after %d attempts; skipping further attempts for this interval", maxPatchAttempts)
                return 
            }
            continue
        }
        actualCandleOpenTime = nextCandleOpenTime
    }
}

func getTheOldestCandleOpenTime(db *sql.DB, pairId uuid.UUID, interval string) (*time.Time, error) {
    query := `SELECT open_time FROM stock_data.candle WHERE pair_id = $1 AND timeframe = $2 ORDER BY open_time ASC LIMIT 1`
    var opentime time.Time
    err := db.QueryRow(query, pairId, interval).Scan(&opentime)
    if err != nil {
        if err == sql.ErrNoRows {
            return nil, nil 
        }
        return nil, err
    }
    return &opentime, nil
}

// getNextCandle returns (nil, sql.ErrNoRows) if not found, otherwise (time, nil) or (nil, otherErr)
func getNextCandleOpenTime(db *sql.DB, pairId uuid.UUID, interval string, nextOpenTime *time.Time) (*time.Time, error) {
    query := `SELECT open_time FROM stock_data.candle WHERE pair_id = $1 AND timeframe = $2 AND open_time = $3`
    var opentime time.Time
    err := db.QueryRow(query, pairId, interval, *nextOpenTime).Scan(&opentime)
    if err != nil {
        if err == sql.ErrNoRows {
            return nil, sql.ErrNoRows
        }
        return nil, err
    }
    return &opentime, nil
}

// patchCandle should return error so caller can react
func patchCandle(db *sql.DB, nextOpenTime *time.Time, symbol string, interval string) error {
    log.Infof("Patching missing candle for time %v", nextOpenTime)
    missingCandle := fetchCandleData(FetchConfig{Symbol: symbol, Interval: interval, Open_Time: nextOpenTime, Limit: 2})
    ids, err := insertCandleData(db, missingCandle, interval, symbol)
    if err != nil {
        return err
    }

    if len(ids) > 0 {
        if perr := publishCandleEvent([]string{"kafka:9092"}, "indicator_events", ids); perr != nil {
            log.WithError(perr).Warn("Could not publish event for patched candle(s)")
            // don't fail the patch on publish error; it's non-critical here
        }
    }
    return nil
}
// calculate next opentime based on duration
// e.g. if duration 1h and current opentime 2024-01-01 10:00 next opentime 2024-01-01 11:00
// it is 10:52 now so next opentime is in future return error
// Additionally, treat the candle as "in-progress" if its close time would be in the future
// (i.e. nextOpenTime + duration > now). In that case, verification should stop
// and not attempt to fetch/insert a still-open candle.
func calculateNextOpenTime(candleTime *time.Time, duration time.Duration) (*time.Time, error) {
	nextOpenTime := candleTime.Add(duration)
	now := time.Now().UTC()
	// if the candle's open time is in the future, stop
	if nextOpenTime.After(now) {
		return nil, fmt.Errorf("next open time %v is in the future", nextOpenTime)
	}
	// if the candle would still be open (close time is in the future), stop
	closeTime := nextOpenTime.Add(duration)
	if closeTime.After(now) {
		return nil, fmt.Errorf("next candle close time %v is in the future (in-progress candle)", closeTime)
	}
	return &nextOpenTime, nil
}
