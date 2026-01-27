package config

import (
	"database/sql"
	"fmt"
	"os"
	"strings"
	"time"

	"github.com/joho/godotenv"
	_ "github.com/lib/pq"
	log "github.com/sirupsen/logrus"
)

type AppConfig struct {
	Symbols   []string
	Intervals []string
	Durations map[string]time.Duration
}

func GetDataBase() (*sql.DB, error) {
	envVars, err := getRequiredEnvVars()
	if err != nil {
		log.WithError(err).Error("Failed to get required environment variables")
		return nil, err
	}

	dbInfo := fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=disable",
		envVars["DB_HOST"], envVars["DB_PORT"], envVars["DB_USER"], envVars["DB_PASSWORD"], envVars["DB_NAME"])

	db, err := sql.Open("postgres", dbInfo)
	if err != nil {
		log.WithError(err).Error("Failed to connect to the database")
		return nil, err
	}

	err = db.Ping()
	if err != nil {
		log.WithError(err).Error("Failed to ping the database")
		return nil, err
	}

	log.Info("Connected to the database successfully")
	db.Exec("SET search_path TO stock_data, public")

	return db, nil
}

func getRequiredEnvVars() (map[string]string, error) {

	if err := godotenv.Load(); err != nil {
		log.WithError(err).Fatal("No .env file found")
	}

	required := []string{"DB_HOST", "DB_PORT", "DB_USER", "DB_PASSWORD", "DB_NAME"}
	envVars := make(map[string]string)
	var missingVars []string

	for _, envVar := range required {
		value := os.Getenv(envVar)
		if value == "" {
			missingVars = append(missingVars, envVar)
		}
		envVars[envVar] = value
	}

	if len(missingVars) > 0 {
		return nil, fmt.Errorf("missing required environment variables: %s", strings.Join(missingVars, ", "))
	}

	return envVars, nil
}

func GetAppConfig() AppConfig {
	supportedSymbols := []string{"ETHUSDC", "BTCUSDC", "SOLUSDC", "ETHBTC"}

	supportedIntervals := []string{"15m", "1h", "4h", "1d"}
	durations := map[string]time.Duration{
		"15m": 15 * time.Minute,
		"1h":  time.Hour,
		"4h":  4 * time.Hour,
		"1d":  24 * time.Hour,
	}

	appConfig := AppConfig{
		Symbols:   supportedSymbols,
		Intervals: supportedIntervals,
		Durations: durations,
	}
	return appConfig
}