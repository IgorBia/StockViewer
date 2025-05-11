package main

import (
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/joho/godotenv"
	_ "github.com/lib/pq"
	log "github.com/sirupsen/logrus"
	"io"
	"net/http"
	"os"
	"strings"
	"time"
)

func init() {

	// Loading environment variables
	if err := godotenv.Load(); err != nil {
		log.WithError(err).Fatal("No .env file found")
	}

	// Setting up the logger
	log.SetFormatter(&log.JSONFormatter{})
	log.SetOutput(os.Stdout)
	log.SetLevel(log.InfoLevel)
}

func getRequiredEnvVars() (map[string]string, error) {
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

func getPairId(db *sql.DB, symbol string) (int, error) {
	var pairId int
	query := `SELECT pair_id FROM pair WHERE symbol = $1`

	err := db.QueryRow(query, symbol).Scan(&pairId)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			log.WithFields(log.Fields{
				"symbol": symbol,
			}).Error("Symbol not found in pair table")
			return 0, fmt.Errorf("symbol %s not found in pair table", symbol)
		}
		log.WithFields(log.Fields{
			"symbol": symbol,
			"error":  err,
		}).Error("Failed to query pair_id")
		return 0, err
	}

	log.WithFields(log.Fields{
		"symbol":  symbol,
		"pair_id": pairId,
	}).Debug("Retrieved pair_id successfully")

	return pairId, nil
}

func fetchCandleData(symbol string, interval string) [][]interface{} {
	// TODO: Implement fetching candle data logic here

	url := fmt.Sprintf("https://api.binance.com/api/v3/klines?symbol=%s&interval=%s&limit=%d", symbol, interval, 1)

	response, err := http.Get(url)
	if err != nil {
		log.WithError(err).Error("Failed request")
	}
	defer func(Body io.ReadCloser) {
		err := Body.Close()
		if err != nil {

		}
	}(response.Body)

	body, err := io.ReadAll(response.Body)
	if err != nil {
		log.WithError(err).Error("Failed to read the response")
	}

	var klines [][]interface{}
	if err := json.Unmarshal(body, &klines); err != nil {
		log.WithError(err).Error("Failed to parse JSON")
	}

	return klines
}

func insertCandleData(db *sql.DB, data [][]interface{}, interval string, symbol string) error {
	// TODO: Implement inserting candle data logic here
	query := `
        INSERT INTO candle (
            pair_id, open_time, open, high, low, close, 
            volume, close_time, quote_volume, trades, 
            taker_base_vol, taker_quote_vol, timeframe
        ) 
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)
    `

	pairId, err := getPairId(db, symbol)
	if err != nil {
		log.WithError(err).Error("Failed to get pair_id")
	}

	tx, err := db.Begin()
	if err != nil {
		log.WithError(err).Error("Failed to begin transaction")
		return err
	}
	defer func(tx *sql.Tx) {
		err := tx.Rollback()
		if err != nil {
			log.WithError(err).Error("Failed to rollback transaction")
		}
	}(tx)

	stmt, err := tx.Prepare(query)
	if err != nil {
		log.WithError(err).Error("Failed to prepare statement")
		return err
	}
	defer func(stmt *sql.Stmt) {
		err := stmt.Close()
		if err != nil {
			log.WithError(err).Error("Failed to close statement")
		}
	}(stmt)

	for _, candle := range data {

		candle[0] = time.UnixMilli(int64(candle[0].(float64))).Format("2006-01-02 15:04:05")
		candle[6] = time.UnixMilli(int64(candle[6].(float64))).Format("2006-01-02 15:04:05")

		_, err = stmt.Exec(
			pairId,
			candle[0],
			candle[1], // open
			candle[2], // high
			candle[3], // low
			candle[4], // close
			candle[5], // volume
			candle[6],
			candle[7],  // quote_volume
			candle[8],  // trades
			candle[9],  // taker_base_vol
			candle[10], // taker_quote_vol
			interval,   // timeframe (e.g., "15m", "1h", "4h", "1d")
		)

		if err != nil {
			log.WithFields(log.Fields{
				"pair_id":   pairId,
				"timeframe": interval,
				"open_time": candle[0],
				"error":     err,
			}).Error("Failed to insert candle")
			return err
		}
	}

	if err = tx.Commit(); err != nil {
		log.WithError(err).Error("Failed to commit transaction")
		return err
	}

	log.WithFields(log.Fields{
		"pair_id":       pairId,
		"timeframe":     interval,
		"candles_count": len(data),
	}).Info("Successfully inserted candle data")

	return nil
}

func scheduleCandleUpdates(db *sql.DB, symbol string, interval string, duration time.Duration) {
	ticker := time.NewTicker(duration)

	go func() {
		for {
			select {
			case <-ticker.C:
				log.Infof("Updating %s for %s", interval, symbol)
				err := insertCandleData(db, fetchCandleData(symbol, interval), interval, symbol)
				if err != nil {
					log.WithError(err).Errorf("Failed to update %s for %s", interval, symbol)
				} else {
					log.Infof("Updated %s for %s", interval, symbol)
				}
			}
		}
	}()
}

func main() {
	envVars, err := getRequiredEnvVars()
	if err != nil {
		log.WithError(err).Fatal("Environment configuration error")
	}

	SupportedSymbols := []string{"ETHUSDC", "BTCUSDC", "SOLUSDC", "ETHBTC"}

	psqlInfo := fmt.Sprintf("host=%s port=%s user=%s "+
		"password=%s dbname=%s sslmode=disable",
		envVars["DB_HOST"], envVars["DB_PORT"], envVars["DB_USER"], envVars["DB_PASSWORD"], envVars["DB_NAME"])

	db, err := sql.Open("postgres", psqlInfo)
	if err != nil {
		log.WithError(err).Fatal("Failed to open database connection")
	}
	defer func(db *sql.DB) {
		err := db.Close()
		if err != nil {
			log.WithError(err).Fatal("Failed to close the database connection")
		}
	}(db)

	err = db.Ping()
	if err != nil {
		log.WithError(err).Fatal("Failed to ping database")
	}

	log.Info("Successfully connected to the database")

	for _, symbol := range SupportedSymbols {
		scheduleCandleUpdates(db, symbol, "1m", 1*time.Minute)
		scheduleCandleUpdates(db, symbol, "15m", 15*time.Minute)
		scheduleCandleUpdates(db, symbol, "4h", 4*time.Hour)
		scheduleCandleUpdates(db, symbol, "1d", 24*time.Hour)
	}

	select {}

}
