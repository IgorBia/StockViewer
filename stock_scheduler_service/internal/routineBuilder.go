package internal

import (
	"database/sql"
	"time"
	"github.com/igorbia/stock_scheduler_service/config"
)

type SchedulerFunc func(db *sql.DB, symbol, interval string, duration time.Duration)

func BuildRoutines(db *sql.DB, scheduler SchedulerFunc, appConfig config.AppConfig) {

    for _, symbol := range appConfig.Symbols {
        for _, interval := range appConfig.Intervals {
            go scheduler(db, symbol, interval, appConfig.Durations[interval])
        }
    }
}
