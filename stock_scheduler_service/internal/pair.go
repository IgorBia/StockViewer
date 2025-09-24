package internal

import (
	"database/sql"
	"errors"
	"fmt"

	"github.com/gofrs/uuid"
	log "github.com/sirupsen/logrus"
)

func getPairId(db *sql.DB, symbol string) (uuid.UUID, error) {
	var pairId uuid.UUID
	query := `SELECT pair_id FROM stock_data.pair WHERE symbol = $1`

	err := db.QueryRow(query, symbol).Scan(&pairId)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			log.WithFields(log.Fields{
				"symbol": symbol,
			}).Error("Symbol not found in pair table")
			return uuid.Nil, fmt.Errorf("symbol %s not found in pair table", symbol)
		}
		log.WithFields(log.Fields{
			"symbol": symbol,
			"error":  err,
		}).Error("Failed to query pair_id")
		return uuid.Nil, err
	}

	log.WithFields(log.Fields{
		"symbol":  symbol,
		"pair_id": pairId,
	}).Debug("Retrieved pair_id successfully")

	return pairId, nil
}
