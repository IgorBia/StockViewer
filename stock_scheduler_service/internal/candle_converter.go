package internal

import (
	"encoding/json"
	"fmt"
	"time"

	"github.com/igorbia/stock_scheduler_service/model"
)

func ConvertRawKlinesToCandles(raw [][]interface{}) []model.Candle {
	candles := make([]model.Candle, 0, len(raw))
	for _, k := range raw {
		if len(k) < 12 {
			continue // pomiń niepełne rekordy
		}
		candle := model.Candle{
			OpenTime:      time.UnixMilli(int64(k[0].(float64))).Format("2006-01-02 15:04:05"),
			Open:          toString(k[1]),
			High:          toString(k[2]),
			Low:           toString(k[3]),
			Close:         toString(k[4]),
			Volume:        toString(k[5]),
			CloseTime:     time.UnixMilli(int64(k[6].(float64))).Format("2006-01-02 15:04:05"),
			QuoteVolume:   toString(k[7]),
			Trades:        toString(k[8]),
			TakerBaseVol:  toString(k[9]),
			TakerQuoteVol: toString(k[10]),
			Ignore:        toString(k[11]),
		}
		candles = append(candles, candle)
	}
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
	default:
		return 0
	}
}

func toString(val interface{}) string {
	switch v := val.(type) {
	case string:
		return v
	case float64:
		return fmt.Sprintf("%v", v)
	case int64:
		return fmt.Sprintf("%v", v)
	case int:
		return fmt.Sprintf("%v", v)
	case json.Number:
		return v.String()
	default:
		return ""
	}
}
