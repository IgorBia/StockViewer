package internal

import (
	"database/sql"
	"time"
	"github.com/igorbia/stock_scheduler_service/config"
    log "github.com/sirupsen/logrus"
)

type SchedulerFunc func(db *sql.DB, symbol, interval string, duration time.Duration)

func BuildRoutines(db *sql.DB, scheduler SchedulerFunc, appConfig config.AppConfig) {

    for _, symbol := range appConfig.Symbols {
        for _, interval := range appConfig.Intervals {
            d, ok := appConfig.Durations[interval]
            if !ok || d <= 0 {
                log.Warnf("Skipping schedule for %s %s: missing or non-positive duration", symbol, interval)
                continue
            }
            go scheduler(db, symbol, interval, d)
        }
    }
}
