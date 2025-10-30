package internal

import (
	"encoding/json"
	"strconv"
	"time"

	"github.com/igorbia/stock_scheduler_service/model"
)

func ConvertRawKlinesToCandles(raw [][]interface{}) []model.Candle {
	candles := make([]model.Candle, 0, len(raw))
	if len(raw) == 0 {
		return candles
	}

	// debug: log first raw kline for inspection
	// log.WithField("raw0", raw[0]).Debug("ConvertRawKlinesToCandles: first raw kline")

	for _, k := range raw {
		if len(k) < 12 {
			continue // pomiń niepełne rekordy
		}

		openMillis := toInt64(k[0])
		closeMillis := toInt64(k[6])

		candle := model.Candle{
			OpenTime:      time.UnixMilli(openMillis).UTC().Format(time.RFC3339),
			Open:          formatFloat(toFloat64(k[1])),
			High:          formatFloat(toFloat64(k[2])),
			Low:           formatFloat(toFloat64(k[3])),
			Close:         formatFloat(toFloat64(k[4])),
			Volume:        formatFloat(toFloat64(k[5])),
			CloseTime:     time.UnixMilli(closeMillis).UTC().Format(time.RFC3339),
			QuoteVolume:   formatFloat(toFloat64(k[7])),
			Trades:        formatInt(k[8]),
			TakerBaseVol:  formatFloat(toFloat64(k[9])),
			TakerQuoteVol: formatFloat(toFloat64(k[10])),
			Ignore:        toString(k[11]),
		}
		candles = append(candles, candle)
	}

	// if len(candles) > 0 {
	// 	log.WithField("first_candle", candles[0]).Debug("ConvertRawKlinesToCandles: first converted candle")
	// }

	return candles
}

func toInt64(val interface{}) int64 {
	switch v := val.(type) {
	case float64:
		return int64(v)
	case int64:
		return v
	case int:
		return int64(v)
	case json.Number:
		i, _ := v.Int64()
		return i
	case string:
		if i, err := strconv.ParseInt(v, 10, 64); err == nil {
			return i
		}
		if f, err := strconv.ParseFloat(v, 64); err == nil {
			return int64(f)
		}
		return 0
	default:
		return 0
	}
}

func toFloat64(val interface{}) float64 {
	switch v := val.(type) {
	case float64:
		return v
	case float32:
		return float64(v)
	case int64:
		return float64(v)
	case int:
		return float64(v)
	case json.Number:
		f, _ := v.Float64()
		return f
	case string:
		f, _ := strconv.ParseFloat(v, 64)
		return f
	default:
		return 0
	}
}

func formatFloat(f float64) string {
	return strconv.FormatFloat(f, 'f', -1, 64)
}

func formatInt(val interface{}) string {
	switch v := val.(type) {
	case float64:
		return strconv.FormatInt(int64(v), 10)
	case int64:
		return strconv.FormatInt(v, 10)
	case int:
		return strconv.Itoa(v)
	case json.Number:
		return v.String()
	case string:
		return v
	default:
		return "0"
	}
}

func toString(val interface{}) string {
	switch v := val.(type) {
	case string:
		return v
	case float64:
		return formatFloat(v)
	case float32:
		return formatFloat(float64(v))
	case int64:
		return strconv.FormatInt(v, 10)
	case int:
		return strconv.Itoa(v)
	case json.Number:
		return v.String()
	default:
		return ""
	}
}
