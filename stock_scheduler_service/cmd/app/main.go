// cmd/app/main.go
package main

import (
	"time"

	"github.com/igorbia/stock_scheduler_service/config"
	"github.com/igorbia/stock_scheduler_service/internal"
	"github.com/igorbia/stock_scheduler_service/log"
)

func main() {
	log.InitLogger()
	db, err := config.GetDataBase()

	SupportedSymbols := []string{"ETHUSDC", "BTCUSDC", "SOLUSDC", "ETHBTC"}

	if err == nil {
		for _, symbol := range SupportedSymbols {
			internal.ScheduleCandleUpdates(db, symbol, "1m", 1*time.Minute)
			internal.ScheduleCandleUpdates(db, symbol, "15m", 15*time.Minute)
			internal.ScheduleCandleUpdates(db, symbol, "4h", 4*time.Hour)
			internal.ScheduleCandleUpdates(db, symbol, "1d", 24*time.Hour)
		}
	}

	select {}
}
