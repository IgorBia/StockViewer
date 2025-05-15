package internal

import (
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
	tests := []struct {
		name         string
		mockResponse string
		config       FetchConfig
		expected     Candle
		expectEmpty  bool
	}{
		{
			name:         "Valid candle data",
			mockResponse: `[ [1620990000000, "60000", "60500", "61000", "59500", "1000", 1620993599999, "60000", "500", "0", "0", "0"] ]`,
			config: FetchConfig{
				Symbol:   "BTCUSDT",
				Interval: "1m",
			},
			expected: Candle{
				Time:      1620990000000,
				Open:      "60000",
				Close:     "60500",
				High:      "61000",
				Low:       "59500",
				Volume:    "1000",
				CloseTime: 1620993599999,
			},
			expectEmpty: false,
		},
		{
			name:         "Empty response",
			mockResponse: `[]`,
			config: FetchConfig{
				Symbol:   "BTCUSDT",
				Interval: "1m",
			},
			expectEmpty: true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
				w.WriteHeader(http.StatusOK)
				_, err := w.Write([]byte(tt.mockResponse))
				if err != nil {
					return
				}
			}))
			defer server.Close()

			tt.config.BaseURL = server.URL
			klines := fetchCandleData(tt.config)

			if tt.expectEmpty {
				if len(klines) != 0 {
					t.Errorf("Expected empty result, got %v", klines)
				}
				return
			}

			if len(klines) != 1 {
				t.Fatalf("Expected 1 candle, got %d", len(klines))
			}

			k := klines[0]
			actual := Candle{
				Time:      int64(k[0].(float64)),
				Open:      k[1].(string),
				Close:     k[2].(string),
				High:      k[3].(string),
				Low:       k[4].(string),
				Volume:    k[5].(string),
				CloseTime: int64(k[6].(float64)),
			}

			if actual != tt.expected {
				t.Errorf("Expected %+v, got %+v", tt.expected, actual)
			}
		})
	}
}
