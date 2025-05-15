package internal

import (
	"database/sql"
	"testing"
	"time"
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

	BuildRoutines(nil, mockScheduler)

	if len(actualCalls) != len(expectedCalls) {
		t.Fatalf("Expected %d calls, but got %d", len(expectedCalls), len(actualCalls))
	}

	for i, expected := range expectedCalls {
		actual := actualCalls[i]
		if actual != expected {
			t.Errorf("Call %d mismatch. Expected %+v, got %+v", i, expected, actual)
		}
	}
}
