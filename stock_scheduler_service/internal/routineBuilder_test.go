package internal

import (
	"database/sql"
	"testing"
	"time"
	"github.com/igorbia/stock_scheduler_service/config"
)

func TestBuildRoutines(t *testing.T) {
	expectedCalls := []struct {
		symbol   string
		interval string
		duration time.Duration
	}{
		{"ETHUSDC", "15m", 15 * time.Minute},
		{"ETHUSDC", "1h", 1 * time.Hour},
		{"ETHUSDC", "4h", 4 * time.Hour},
		{"ETHUSDC", "1d", 24 * time.Hour},
		{"BTCUSDC", "15m", 15 * time.Minute},
		{"BTCUSDC", "1h", 1 * time.Hour},
		{"BTCUSDC", "4h", 4 * time.Hour},
		{"BTCUSDC", "1d", 24 * time.Hour},
		{"SOLUSDC", "15m", 15 * time.Minute},
		{"SOLUSDC", "1h", 1 * time.Hour},
		{"SOLUSDC", "4h", 4 * time.Hour},
		{"SOLUSDC", "1d", 24 * time.Hour},
		{"ETHBTC", "15m", 15 * time.Minute},
		{"ETHBTC", "1h", 1 * time.Hour},
		{"ETHBTC", "4h", 4 * time.Hour},
		{"ETHBTC", "1d", 24 * time.Hour},
	}

	var actualCalls []struct {
		symbol   string
		interval string
		duration time.Duration
	}

	

	mockScheduler := func(db *sql.DB, symbol, interval string, duration time.Duration) {
		actualCalls = append(actualCalls, struct {
			symbol   string
			interval string
			duration time.Duration
		}{symbol, interval, duration})
	}

	appConfig := config.AppConfig{
		Symbols:   []string{"ETHUSDC", "BTCUSDC", "SOLUSDC", "ETHBTC"},
		Intervals: []string{"15m", "1h", "4h", "1d"},
		Durations: map[string]time.Duration{
			"15m": 15 * time.Minute,
			"1h":  1 * time.Hour,
			"4h":  4 * time.Hour,
			"1d":  24 * time.Hour,
		},
	}

	// Call BuildRoutines with the mock scheduler

	BuildRoutines(nil, mockScheduler, appConfig)

	deadline := time.Now().Add(200 * time.Millisecond)
	for len(actualCalls) < len(expectedCalls) && time.Now().Before(deadline) {
		time.Sleep(5 * time.Millisecond)
	}

	if len(actualCalls) != len(expectedCalls) {
		t.Fatalf("Expected %d calls, but got %d", len(expectedCalls), len(actualCalls))
	}

	// Compare as sets (order-independent): map key = symbol|interval -> duration
	expectedMap := make(map[string]time.Duration)
	for _, e := range expectedCalls {
		key := e.symbol + "|" + e.interval
		expectedMap[key] = e.duration
	}

	actualMap := make(map[string]time.Duration)
	for _, a := range actualCalls {
		key := a.symbol + "|" + a.interval
		actualMap[key] = a.duration
	}

	if len(actualMap) != len(expectedMap) {
		t.Fatalf("Expected %d unique calls, but got %d", len(expectedMap), len(actualMap))
	}

	for k, ed := range expectedMap {
		ad, ok := actualMap[k]
		if !ok {
			t.Errorf("Missing expected call: %s", k)
			continue
		}
		if ad != ed {
			t.Errorf("Duration mismatch for %s: expected %v, got %v", k, ed, ad)
		}
	}
}
