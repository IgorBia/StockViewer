package internal

import (
	"errors"
	"regexp"
	"testing"
	"time"

	"github.com/DATA-DOG/go-sqlmock"
	"github.com/igorbia/stock_scheduler_service/model"
)

func TestInsertCandleData(t *testing.T) {
	type args struct {
		data     []model.Candle
		interval string
		symbol   string
	}

	tests := []struct {
		name      string
		args      args
		mockSetup func(mock sqlmock.Sqlmock)
		wantErr   bool
	}{
		{
			name: "successfully insert one candle",
			args: args{
				data: []model.Candle{
					{
						OpenTime:      time.UnixMilli(1620990000000).Format("2006-01-02 15:04:05"),
						Open:          "60000",
						High:          "61000",
						Low:           "59000",
						Close:         "60500",
						Volume:        "1000",
						CloseTime:     time.UnixMilli(1620993599999).Format("2006-01-02 15:04:05"),
						QuoteVolume:   "60000000",
						Trades:        "500",
						TakerBaseVol:  "0",
						TakerQuoteVol: "0",
						Ignore:        "0",
					},
				},
				interval: "1m",
				symbol:   "BTCUSDT",
			},
			mockSetup: func(mock sqlmock.Sqlmock) {
				mock.ExpectQuery(regexp.QuoteMeta("SELECT pair_id FROM stock_data.pair WHERE symbol = $1")).
					WithArgs("BTCUSDT").
					WillReturnRows(sqlmock.NewRows([]string{"pair_id"}).AddRow("11111111-1111-1111-1111-111111111111"))

				mock.ExpectBegin()

				mock.ExpectQuery(regexp.QuoteMeta(
					"INSERT INTO stock_data.candle (            pair_id, open_time, open, high, low, close,             volume, close_time, quote_volume, trades,             taker_base_vol, taker_quote_vol, timeframe         )          VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)        ON CONFLICT (pair_id, timeframe, close_time) DO NOTHING        RETURNING candle_id",
				)).
					WithArgs(
						"11111111-1111-1111-1111-111111111111",
						time.UnixMilli(1620990000000).Format("2006-01-02 15:04:05"), "60000", "61000", "59000", "60500",
						"1000", time.UnixMilli(1620993599999).Format("2006-01-02 15:04:05"), "60000000", "500", "0", "0", "1m",
					).
					WillReturnRows(sqlmock.NewRows([]string{"candle_id"}).AddRow("22222222-2222-2222-2222-222222222222"))

				mock.ExpectCommit()
			},

			wantErr: false,
		},
		{
			name: "fail on insert exec",
			args: args{
				data: []model.Candle{
					{
						OpenTime:      time.UnixMilli(1620990000000).Format("2006-01-02 15:04:05"),
						Open:          "60000",
						High:          "61000",
						Low:           "59000",
						Close:         "60500",
						Volume:        "1000",
						CloseTime:     time.UnixMilli(1620993599999).Format("2006-01-02 15:04:05"),
						QuoteVolume:   "60000000",
						Trades:        "500",
						TakerBaseVol:  "0",
						TakerQuoteVol: "0",
						Ignore:        "0",
					},
				},
				interval: "1m",
				symbol:   "BTCUSDT",
			},
			mockSetup: func(mock sqlmock.Sqlmock) {
				mock.ExpectQuery(regexp.QuoteMeta("SELECT pair_id FROM stock_data.pair WHERE symbol = $1")).
					WithArgs("BTCUSDT").
					WillReturnRows(sqlmock.NewRows([]string{"pair_id"}).AddRow("11111111-1111-1111-1111-111111111111"))

				mock.ExpectBegin()

				mock.ExpectQuery(regexp.QuoteMeta(
					"INSERT INTO stock_data.candle (            pair_id, open_time, open, high, low, close,             volume, close_time, quote_volume, trades,             taker_base_vol, taker_quote_vol, timeframe         )          VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)        ON CONFLICT (pair_id, timeframe, close_time) DO NOTHING        RETURNING candle_id",
				)).WillReturnError(errors.New("insert failed"))

				mock.ExpectRollback()
			},
			wantErr: true,
		},
		{
			name: "fail on finding pair",
			args: args{
				data: []model.Candle{
					{
						OpenTime:      time.UnixMilli(1620990000000).Format("2006-01-02 15:04:05"),
						Open:          "60000",
						High:          "61000",
						Low:           "59000",
						Close:         "60500",
						Volume:        "1000",
						CloseTime:     time.UnixMilli(1620993599999).Format("2006-01-02 15:04:05"),
						QuoteVolume:   "60000000",
						Trades:        "500",
						TakerBaseVol:  "0",
						TakerQuoteVol: "0",
						Ignore:        "0",
					},
				},
				interval: "1m",
				symbol:   "XYZ",
			},
			mockSetup: func(mock sqlmock.Sqlmock) {
				mock.ExpectQuery(regexp.QuoteMeta("SELECT pair_id FROM stock_data.pair WHERE symbol = $1")).
					WithArgs("XYZ").
					WillReturnError(errors.New("no pair found"))
			},
			wantErr: true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			db, mock, err := sqlmock.New()
			if err != nil {
				t.Fatalf("error initializing sqlmock: %v", err)
			}
			defer db.Close()

			tt.mockSetup(mock)

			_, err = insertCandleData(db, tt.args.data, tt.args.interval, tt.args.symbol)
			if (err != nil) != tt.wantErr {
				t.Errorf("insertCandleData() error = %v, wantErr %v", err, tt.wantErr)
			}

			if err := mock.ExpectationsWereMet(); err != nil {
				t.Errorf("unfulfilled expectations: %v", err)
			}
		})
	}
}
