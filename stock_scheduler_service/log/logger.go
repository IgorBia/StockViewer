package log

import (
	log "github.com/sirupsen/logrus"
	"os"
)

func InitLogger() {
	log.SetFormatter(&log.JSONFormatter{})
	log.SetOutput(os.Stdout)
	log.SetLevel(log.DebugLevel)
}
