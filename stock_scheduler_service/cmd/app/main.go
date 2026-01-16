package main

import (
	"fmt"
	"github.com/igorbia/stock_scheduler_service/config"
	"github.com/igorbia/stock_scheduler_service/internal"
	"github.com/igorbia/stock_scheduler_service/log"
)

func main() {
	fmt.Print("__     __ _             _    __      _              _       _            __  \n\\ \\   / _| |_ ___   ___| | _/ _\\ ___| |__   ___  __| |_   _| | ___ _ __  \\ \\ \n \\ \\  \\ \\| __/ _ \\ / __| |/ \\ \\ / __| '_ \\ / _ \\/ _` | | | | |/ _ | '__|  \\ \\\n / /  _\\ | || (_) | (__|   <_\\ | (__| | | |  __| (_| | |_| | |  __| |     / /\n/_/   \\__/\\__\\___/ \\___|_|\\_\\__/\\___|_| |_|\\___|\\__,_|\\__,_|_|\\___|_|    /_/ \n\n")
	log.InitLogger()
	db, err := config.GetDataBase()


	if err == nil {
		internal.SetupDB(db)
		internal.EnsureDataIntegrity(db, config.GetAppConfig())
		internal.BuildRoutines(db, internal.ScheduleCandleUpdates, config.GetAppConfig())
	}

	select {}
}
