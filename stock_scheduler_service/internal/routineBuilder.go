package internal

import (
	"database/sql"
	"time"
)

func BuildRoutines(db *sql.DB) {

	SupportedSymbols := []string{"ETHUSDC", "BTCUSDC", "SOLUSDC", "ETHBTC"}

	for _, symbol := range SupportedSymbols {
		ScheduleCandleUpdates(db, symbol, "15m", 15*time.Minute)
		ScheduleCandleUpdates(db, symbol, "1h", 1*time.Hour)
		ScheduleCandleUpdates(db, symbol, "4h", 4*time.Hour)
		ScheduleCandleUpdates(db, symbol, "1d", 24*time.Hour)
	}
}
