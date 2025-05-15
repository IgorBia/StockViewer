package internal

import (
	"encoding/json"
	"io"
	"net/http"
	"net/url"

	log "github.com/sirupsen/logrus"
)

type FetchConfig struct {
	BaseURL  string
	Symbol   string
	Interval string
}

func buildURL(base string, symbol string, interval string) (*url.URL, error) {

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
		return nil, nil
	}

	params := url.Values{}
	params.Add("symbol", symbol)
	params.Add("interval", interval)
	params.Add("limit", "1")

	parsedURL, err := url.Parse(base)
	if err != nil {
		log.WithError(err).Error("Failed to parse URL")
		return nil, err
	}
	parsedURL.RawQuery = params.Encode()

	return parsedURL, nil
}

func fetchCandleData(cfg FetchConfig) [][]interface{} {

	if cfg.BaseURL == "" {
		cfg.BaseURL = "https://api.binance.com/api/v3/klines"
	}

	apiURL, err := buildURL(cfg.BaseURL, cfg.Symbol, cfg.Interval)
	if apiURL == nil {
		log.Error("Failed to build URL")
		return nil
	}
	response, err := http.Get(apiURL.String())
	if err != nil {
		log.WithError(err).Error("Failed request")
		return nil
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
