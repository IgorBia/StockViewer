package internal

import (
	"database/sql"
	"time"

	log "github.com/sirupsen/logrus"
)

func insertCandleData(db *sql.DB, data [][]interface{}, interval string, symbol string) error {
	query := `
        INSERT INTO stock_data.candle (
            pair_id, open_time, open, high, low, close, 
            volume, close_time, quote_volume, trades, 
            taker_base_vol, taker_quote_vol, timeframe
        ) 
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)
    `

	pairId, err := getPairId(db, symbol)
	if err != nil {
		log.WithError(err).Error("Failed to get pair_id")
		return err
	}

	tx, err := db.Begin()
	if err != nil {
		log.WithError(err).Error("Failed to begin transaction")
		return err
	}

	stmt, err := tx.Prepare(query)
	if err != nil {
		log.WithError(err).Error("Failed to prepare statement")
		return err
	}
	defer func(stmt *sql.Stmt) {
		err := stmt.Close()
		if err != nil {
			log.WithError(err).Error("Failed to close statement")
		}
	}(stmt)

	for _, candle := range data {

		candle[0] = time.UnixMilli(int64(candle[0].(float64))).Format("2006-01-02 15:04:05")
		candle[6] = time.UnixMilli(int64(candle[6].(float64))).Format("2006-01-02 15:04:05")

		_, err = stmt.Exec(
			pairId,
			candle[0],
			candle[1], // open
			candle[2], // high
			candle[3], // low
			candle[4], // close
			candle[5], // volume
			candle[6],
			candle[7],  // quote_volume
			candle[8],  // trades
			candle[9],  // taker_base_vol
			candle[10], // taker_quote_vol
			interval,   // timeframe (e.g., "15m", "1h", "4h", "1d")
		)

		if err != nil {
			_ = tx.Rollback()
			log.WithFields(log.Fields{
				"pair_id":   pairId,
				"timeframe": interval,
				"open_time": candle[0],
				"error":     err,
			}).Error("Failed to insert candle")
			return err
		}
	}

	if err = tx.Commit(); err != nil {
		log.WithError(err).Error("Failed to commit transaction")
		_ = tx.Rollback()
		return err
	}

	log.WithFields(log.Fields{
		"pair_id":       pairId,
		"timeframe":     interval,
		"candles_count": len(data),
	}).Info("Successfully inserted candle data")

	return nil
}
