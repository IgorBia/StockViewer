package internal

import (
	"database/sql"
	_ "time"

	"github.com/gofrs/uuid"
	"github.com/igorbia/stock_scheduler_service/model"
	log "github.com/sirupsen/logrus"

	_ "github.com/sirupsen/logrus"
)

func insertCandleData(db *sql.DB, data []model.Candle, interval string, symbol string) ([]uuid.UUID, error) {
	ids := make([]uuid.UUID, 0, len(data))
	query := `
        INSERT INTO stock_data.candle (
            pair_id, open_time, open, high, low, close, 
            volume, close_time, quote_volume, trades, 
            taker_base_vol, taker_quote_vol, timeframe
        ) 
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)
		ON CONFLICT (pair_id, timeframe, close_time) DO NOTHING
		RETURNING candle_id`

	pairId, err := getPairId(db, symbol)
	if err != nil {
		log.WithError(err).Error("Failed to get pair_id")
		return nil, nil
	}

	tx, err := db.Begin()
	if err != nil {
		log.WithError(err).Error("Failed to begin transaction")
		return nil, err
	}

	stmt, err := tx.Prepare(query)
	if err != nil {
		log.WithError(err).Error("Failed to prepare statement")
		return nil, err
	}
	defer func(stmt *sql.Stmt) {
		err := stmt.Close()
		if err != nil {
			log.WithError(err).Error("Failed to close statement")
		}
	}(stmt)

	for _, c := range data {
		var id uuid.UUID
		err := db.QueryRow(query,
			pairId, c.OpenTime, c.Open, c.High, c.Low, c.Close, c.Volume, c.CloseTime,
			c.QuoteVolume, c.Trades, c.TakerBaseVol, c.TakerQuoteVol, interval,
		).Scan(&id)
		if err != nil {
			_ = tx.Rollback()
			log.WithFields(log.Fields{
				"pair_id":   pairId,
				"timeframe": interval,
				"open_time": c.OpenTime,
				"error":     err,
			}).Error("Failed to insert candle")
			return nil, err
		}
		ids = append(ids, id)
	}

	if err = tx.Commit(); err != nil {
		log.WithError(err).Error("Failed to commit transaction")
		_ = tx.Rollback()
		return nil, err
	}

	log.WithFields(log.Fields{
		"pair_id":       pairId,
		"timeframe":     interval,
		"candles_count": len(data),
	}).Info("Successfully inserted candle data")

	return ids, nil
}
