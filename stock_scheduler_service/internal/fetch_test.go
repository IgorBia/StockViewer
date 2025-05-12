package internal

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"net/url"
	"testing"
)

type Candle struct {
	Time      int64  `json:"time"`
	Open      string `json:"open"`
	Close     string `json:"close"`
	High      string `json:"high"`
	Low       string `json:"low"`
	Volume    string `json:"volume"`
	CloseTime int64  `json:"closeTime"`
}

func TestBuildURL(t *testing.T) {
	tests := []struct {
		name        string
		base        string
		symbol      string
		interval    string
		expectNil   bool
		expectErr   bool
		expectedURL *url.URL
	}{
		{
			name:      "Normal case",
			base:      "https://api.binance.com/api/v3/klines",
			symbol:    "BTCUSDT",
			interval:  "1m",
			expectNil: false,
			expectErr: false,
			expectedURL: &url.URL{
				Scheme:   "https",
				Host:     "api.binance.com",
				Path:     "/api/v3/klines",
				RawQuery: "interval=1m&limit=1&symbol=BTCUSDT",
			},
		},
		{
			name:      "Empty base",
			base:      "",
			symbol:    "BTCUSDT",
			interval:  "1m",
			expectNil: true,
			expectErr: false,
		},
		{
			name:      "Empty symbol",
			base:      "https://api.binance.com/api/v3/klines",
			symbol:    "",
			interval:  "1m",
			expectNil: true,
			expectErr: false,
		},
		{
			name:      "Empty interval",
			base:      "https://api.binance.com/api/v3/klines",
			symbol:    "BTCUSDT",
			interval:  "",
			expectNil: true,
			expectErr: false,
		},
		{
			name:      "All empty",
			base:      "",
			symbol:    "",
			interval:  "",
			expectNil: true,
			expectErr: false,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			// Execute the buildURL function
			resultURL, err := buildURL(tt.base, tt.symbol, tt.interval)

			if tt.expectNil {
				if resultURL != nil {
					t.Errorf("Expected nil URL, got %v", resultURL)
				}
				if err != nil && !tt.expectErr {
					t.Errorf("Expected no error, got %v", err)
				}
			} else {
				if resultURL == nil {
					t.Errorf("Expected non-nil URL, got nil")
				} else if resultURL.String() != tt.expectedURL.String() {
					t.Errorf("Expected URL %v, got %v", tt.expectedURL, resultURL)
				}
			}
		})
	}
}

func TestFetchCandleData(t *testing.T) {
	// Mockowanie odpowiedzi serwera
	mockResponse := `[ [1620990000000, "60000", "60500", "61000", "59500", "1000", 1620993599999, "60000", "500", "0", "0", "0"] ]`
	handler := func(w http.ResponseWriter, r *http.Request) {
		// Symulujemy odpowiedź serwera
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(mockResponse))
	}
	server := httptest.NewServer(http.HandlerFunc(handler))
	defer server.Close()

	// Zamień URL w funkcji buildURL na ten mockowy serwer
	_, err := buildURL(server.URL, "BTCUSDT", "1m")
	if err != nil {
		t.Fatalf("Failed to build URL: %v", err)
	}

	// Wywołaj funkcję fetchCandleData
	klines := fetchCandleData("BTCUSDT", "1m")

	// Sprawdzamy, czy dane zostały poprawnie sparsowane
	if len(klines) == 0 {
		t.Errorf("Expected klines data, got empty")
	}

	// Sprawdzamy, czy dane świecy są poprawne
	expectedCandle := Candle{
		Time:      1620990000000,
		Open:      "60000",
		Close:     "60500",
		High:      "61000",
		Low:       "59500",
		Volume:    "1000",
		CloseTime: 1620993599999,
	}

	var actualCandle Candle
	if err := json.Unmarshal([]byte(mockResponse), &actualCandle); err != nil {
		t.Fatalf("Failed to unmarshal mock response: %v", err)
	}

	if actualCandle != expectedCandle {
		t.Errorf("Expected candle %v, but got %v", expectedCandle, actualCandle)
	}
}
