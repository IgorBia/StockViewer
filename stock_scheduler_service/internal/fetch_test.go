package internal

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/igorbia/stock_scheduler_service/model"
)

func TestBuildURL(t *testing.T) {
	tests := []struct {
		name      string
		base      string
		symbol    string
		interval  string
		expectNil bool
		expectErr bool
	}{
		{
			name:      "Normal case",
			base:      "https://api.binance.com/api/v3/klines",
			symbol:    "BTCUSDT",
			interval:  "1m",
			expectNil: false,
			expectErr: false,
		},
		{
			name:      "Empty base",
			base:      "",
			symbol:    "BTCUSDT",
			interval:  "1m",
			expectNil: true,
			expectErr: true,
		},
		{
			name:      "Empty symbol",
			base:      "https://api.binance.com/api/v3/klines",
			symbol:    "",
			interval:  "1m",
			expectNil: true,
			expectErr: true,
		},
		{
			name:      "Empty interval",
			base:      "https://api.binance.com/api/v3/klines",
			symbol:    "BTCUSDT",
			interval:  "",
			expectNil: true,
			expectErr: true,
		},
		{
			name:      "All empty",
			base:      "",
			symbol:    "",
			interval:  "",
			expectNil: true,
			expectErr: true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			resultURL, err := buildURL(tt.base, tt.symbol, tt.interval, nil, 1)

			if tt.expectNil {
				if resultURL != nil {
					t.Errorf("Expected nil URL, got %v", resultURL)
				}
				if err == nil && tt.expectErr {
					t.Errorf("Expected an error, got nil")
				}
			} else {
				if resultURL == nil {
					t.Fatalf("Expected non-nil URL, got nil")
				}
				if resultURL.Scheme != "https" {
					t.Errorf("Expected scheme https, got %s", resultURL.Scheme)
				}
				if resultURL.Host != "api.binance.com" {
					t.Errorf("Expected host api.binance.com, got %s", resultURL.Host)
				}
				if resultURL.Path != "/api/v3/klines" {
					t.Errorf("Expected path /api/v3/klines, got %s", resultURL.Path)
				}
				q := resultURL.Query()
				if q.Get("symbol") != "BTCUSDT" {
					t.Errorf("Expected symbol=BTCUSDT, got %s", q.Get("symbol"))
				}
				if q.Get("interval") != "1m" {
					t.Errorf("Expected interval=1m, got %s", q.Get("interval"))
				}
				if q.Get("limit") != "1" {
					t.Errorf("Expected limit=1, got %s", q.Get("limit"))
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
		expected     model.Candle
		expectEmpty  bool
	}{
		{
			name: "Valid candle data",
			mockResponse: `[ [1620990000000, "60000", "61000", "59500", "60500", "1000", 1620993599999, "60000000", 500, "0", "0", "0"] ]`,
			config: FetchConfig{
				Symbol:   "BTCUSDT",
				Interval: "1m",
				Limit:    1,
			},
			expected: model.Candle{
				OpenTime:  time.UnixMilli(1620990000000).UTC().Format(time.RFC3339),
				Open:      "60000",
				Close:     "60500",
				High:      "61000",
				Low:       "59500",
				Volume:    "1000",
				CloseTime: time.UnixMilli(1620993599999).UTC().Format(time.RFC3339),
			},
			expectEmpty: false,
		},
		{
			name:         "Empty response",
			mockResponse: `[]`,
			config: FetchConfig{
				Symbol:   "BTCUSDT",
				Interval: "1m",
				Limit:    1,
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

			if k.OpenTime != tt.expected.OpenTime {
				t.Errorf("OpenTime mismatch: expected %s got %s", tt.expected.OpenTime, k.OpenTime)
			}
			if k.Open != tt.expected.Open {
				t.Errorf("Open mismatch: expected %s got %s", tt.expected.Open, k.Open)
			}
			if k.Close != tt.expected.Close {
				t.Errorf("Close mismatch: expected %s got %s", tt.expected.Close, k.Close)
			}
			if k.High != tt.expected.High {
				t.Errorf("High mismatch: expected %s got %s", tt.expected.High, k.High)
			}
			if k.Low != tt.expected.Low {
				t.Errorf("Low mismatch: expected %s got %s", tt.expected.Low, k.Low)
			}
			if k.Volume != tt.expected.Volume {
				t.Errorf("Volume mismatch: expected %s got %s", tt.expected.Volume, k.Volume)
			}
			if k.CloseTime != tt.expected.CloseTime {
				t.Errorf("CloseTime mismatch: expected %s got %s", tt.expected.CloseTime, k.CloseTime)
			}
		})
	}
}
