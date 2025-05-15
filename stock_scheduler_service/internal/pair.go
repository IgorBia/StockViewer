package internal

import (
	"database/sql"
	"errors"
	"fmt"

	log "github.com/sirupsen/logrus"
)

func getPairId(db *sql.DB, symbol string) (int, error) {
	var pairId int
	query := `SELECT pair_id FROM pair WHERE symbol = $1`

	err := db.QueryRow(query, symbol).Scan(&pairId)
	if err != nil || pairId == 0 {
		if errors.Is(err, sql.ErrNoRows) {
			log.WithFields(log.Fields{
				"symbol": symbol,
			}).Error("Symbol not found in pair table")
			return 0, fmt.Errorf("symbol %s not found in pair table", symbol)
		}
		log.WithFields(log.Fields{
			"symbol": symbol,
			"error":  err,
		}).Error("Failed to query pair_id")
		return 0, err
	}

	log.WithFields(log.Fields{
		"symbol":  symbol,
		"pair_id": pairId,
	}).Debug("Retrieved pair_id successfully")

	return pairId, nil
}
