package internal

import (
	"database/sql"
	"time"
	log "github.com/sirupsen/logrus"
	"github.com/gofrs/uuid"
	"fmt"
)
/*
Data Integration verification:
	For every symbol and timeframe:
		1. Get the oldest candle
		2. Get the next candle if not in future
		3. If missing fetch the candle 
		4. Repeat step 2 until next candle in future
*/
func EnsureDataIntegrity(db *sql.DB){
	log.Info("Starting data integrity verification routines for all supported symbols and intervals.")
	SupportedSymbols := []string{"ETHUSDC", "BTCUSDC", "SOLUSDC", "ETHBTC"}

	for _, symbol := range SupportedSymbols {
		go ensureDataIntegrityForSymbolAndInterval(db, symbol, "15m", 15*time.Minute)
		go ensureDataIntegrityForSymbolAndInterval(db, symbol, "1h", 1*time.Hour)
		go ensureDataIntegrityForSymbolAndInterval(db, symbol, "4h", 4*time.Hour)
		go ensureDataIntegrityForSymbolAndInterval(db, symbol, "1d", 24*time.Hour)
	}
}
func ensureDataIntegrityForSymbolAndInterval(db *sql.DB, symbol string, interval string, duration time.Duration){
	log.Infof("Starting data integrity verification for %s %s", symbol, interval)
	pairId, err := getPairId(db, symbol)
	if err != nil {
		log.WithFields(log.Fields{
			"symbol": symbol,
		}).Error("Failed to get pair ID")
		return
	}
	actualCandle := getTheOldestCandle(db, pairId, interval)
	if actualCandle==nil{
		return; 	// db is empty therefore integral
	}
	for true {
		nextOpenTime, err := calculateNextOpenTime(actualCandle, duration)
		if err != nil {
			log.Info("Data integrity verified for %s %s, all candles present up to current time.", symbol, interval)
			return // db is integral = error opentime is in future - oldest to now checked
		}
		actualCandle, err = getNextCandle(db, pairId, interval, nextOpenTime)
		if actualCandle==nil && err!=nil {
			patchCandle(db, nextOpenTime, symbol, interval)
		}
		actualCandle = nextOpenTime
	}
}

func getTheOldestCandle(db *sql.DB, pairId uuid.UUID, interval string) *time.Time {
	query := `SELECT open_time FROM stock_data.candle WHERE pair_id = $1 AND timeframe = $2 ORDER BY open_time ASC LIMIT 1`
	var opentime time.Time

	err := db.QueryRow(query, pairId, interval).Scan(&opentime)
	if err != nil {
		if err == sql.ErrNoRows {
			log.WithFields(log.Fields{
				"pairId":  pairId,
				"interval": interval,
			}).Info("No candles found for symbol and interval")
			return nil
		}
	}
	return &opentime
}

func calculateNextOpenTime(candleTime *time.Time, duration time.Duration) (*time.Time, error) {
	nextOpenTime := candleTime.Add(duration)
	if nextOpenTime.After(time.Now()) {
		return nil, fmt.Errorf("next open time %v is in the future", nextOpenTime)
	}
	return &nextOpenTime, nil
}
func getNextCandle(db *sql.DB, pairId uuid.UUID, interval string, nextOpenTime *time.Time) (*time.Time, error) {
	query := `SELECT open_time FROM stock_data.candle WHERE pair_id = $1 AND timeframe = $2 AND open_time = $3`
	var opentime time.Time

	err := db.QueryRow(query, pairId, interval, nextOpenTime).Scan(&opentime)
	if err != nil {
		if err == sql.ErrNoRows {
			log.WithFields(log.Fields{
				"pairId":  pairId,
				"interval": interval,
			}).Info("No next candle found")
			return nil, fmt.Errorf("no candle found for next open time %v", nextOpenTime)
		}
	}
	return &opentime, nil
}

// 	//get opentime from current and calculate next opentime then select from db the next candle
func patchCandle(db *sql.DB, nextOpenTime *time.Time, symbol string, interval string){
	log.Infof("Patching missing candle for time %v", nextOpenTime)
	missingCandle := fetchCandleData(FetchConfig{Symbol: symbol, Interval: interval, Open_Time: nextOpenTime, Limit:1})
	ids, err := insertCandleData(db, missingCandle, interval, symbol)
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
	// call for api with next candle open time
}