package internal

import (
	"encoding/json"
	"io"
	"net/http"
	"net/url"

	log "github.com/sirupsen/logrus"
)

func fetchCandleData(symbol string, interval string) [][]interface{} {
	// TODO: Implement fetching candle data logic here

	baseURL := "https://api.binance.com/api/v3/klines"
	params := url.Values{}
	params.Add("symbol", symbol)
	params.Add("interval", interval)
	params.Add("limit", "1")

	parsedURL, err := url.Parse(baseURL)
	if err != nil {
		log.WithError(err).Error("Failed to parse URL")
		return nil
	}
	parsedURL.RawQuery = params.Encode()

	response, err := http.Get(parsedURL.String())
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
