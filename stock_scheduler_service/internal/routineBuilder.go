package internal

import (
	"database/sql"
	"time"
)

type SchedulerFunc func(db *sql.DB, symbol, interval string, duration time.Duration)

func BuildRoutines(db *sql.DB, scheduler SchedulerFunc) {

	SupportedSymbols := []string{"ETHUSDC", "BTCUSDC", "SOLUSDC", "ETHBTC"}

	for _, symbol := range SupportedSymbols {
		go scheduler(db, symbol, "1s", 1*time.Second)
		go scheduler(db, symbol, "1h", 1*time.Hour)
		go scheduler(db, symbol, "4h", 4*time.Hour)
		go scheduler(db, symbol, "1d", 24*time.Hour)
	}
}
