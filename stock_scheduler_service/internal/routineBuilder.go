package internal

import (
	"database/sql"
	"time"
)

type SchedulerFunc func(db *sql.DB, symbol, interval string, duration time.Duration)

func BuildRoutines(db *sql.DB, scheduler SchedulerFunc) {

	SupportedSymbols := []string{"ETHUSDC", "BTCUSDC", "SOLUSDC", "ETHBTC"}

	for _, symbol := range SupportedSymbols {
		scheduler(db, symbol, "15m", 15*time.Minute)
		scheduler(db, symbol, "1h", 1*time.Hour)
		scheduler(db, symbol, "4h", 4*time.Hour)
		scheduler(db, symbol, "1d", 24*time.Hour)
	}
}
