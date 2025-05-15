package main

import (
	"github.com/igorbia/stock_scheduler_service/config"
	"github.com/igorbia/stock_scheduler_service/internal"
	"github.com/igorbia/stock_scheduler_service/log"
)

func main() {
	log.InitLogger()
	db, err := config.GetDataBase()

	if err == nil {
		internal.BuildRoutines(db, internal.ScheduleCandleUpdates)
	}

	select {}
}
