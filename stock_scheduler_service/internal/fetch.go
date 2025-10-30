package internal

import (
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"net/url"
	"time"
	"fmt"

	"github.com/igorbia/stock_scheduler_service/model"
	log "github.com/sirupsen/logrus"
)

type FetchConfig struct {
	BaseURL  string
	Symbol   string
	Interval string
	Open_Time *time.Time
	Limit	int
}

func buildURL(base string, symbol string, interval string, openTime *time.Time, limit int) (*url.URL, error) {

	if base == "" {
		log.Error("Base URL is empty")
	}
	if symbol == "" {
		log.Error("Symbol is empty")
	}
	if interval == "" {
		log.Error("Interval is empty")
	}

	if base == "" || symbol == "" || interval == "" {
		return nil, errors.New("missing base/symbol/interval")
	}

	params := url.Values{}
	params.Add("symbol", symbol)
	params.Add("interval", interval)
	params.Add("limit", fmt.Sprintf("%d", limit))
	if openTime != nil {
		params.Add("startTime", fmt.Sprintf("%d", openTime.UnixMilli()))
	}

	parsedURL, err := url.Parse(base)
	if err != nil {
		log.WithError(err).Error("Failed to parse URL")
		return nil, err
	}
	parsedURL.RawQuery = params.Encode()

	return parsedURL, nil
}

func fetchCandleData(cfg FetchConfig) []model.Candle {

	if cfg.BaseURL == "" {
		cfg.BaseURL = "https://api.binance.com/api/v3/klines"
	}
	var apiURL *url.URL
	var err error
	if( cfg.Open_Time != nil ){
		apiURL, err = buildURL(cfg.BaseURL, cfg.Symbol, cfg.Interval, cfg.Open_Time, cfg.Limit)
	} else {
		apiURL, err = buildURL(cfg.BaseURL, cfg.Symbol, cfg.Interval, nil, cfg.Limit)
	}
	if err != nil {
		log.WithError(err).Error("Failed to build URL")
		return nil
	}
	response, err := http.Get(apiURL.String())
	if err != nil {
		log.WithError(err).Error("Failed request")
		return nil
	}
	defer func() {
		if cerr := response.Body.Close(); cerr != nil {
			log.WithError(cerr).Warn("Failed to close response body")
		}
	}()

	if response.StatusCode != http.StatusOK {
		bodyBytes, _ := io.ReadAll(response.Body)
		log.WithFields(log.Fields{
			"status": response.StatusCode,
			"body":   string(bodyBytes),
		}).Error("Non-200 response from API")
		return nil
	}
	log.Info(response.Body)

	// Use Decoder + UseNumber to preserve numeric types
	decoder := json.NewDecoder(response.Body)
	decoder.UseNumber()
	var klines [][]interface{}
	if err := decoder.Decode(&klines); err != nil {
		log.WithError(err).Error("Failed to decode JSON response")
		return nil
	}

	if len(klines) == 0 {
		log.Infof("No klines returned for %s %s", cfg.Symbol, cfg.Interval)
		return nil
	}

	var kline [][]interface{}
	kline = append(kline, klines[0])

	return ConvertRawKlinesToCandles(kline)
}
