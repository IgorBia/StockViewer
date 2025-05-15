package internal

import (
	_ "database/sql"
	"errors"
	"regexp"
	"testing"
	"time"

	"github.com/DATA-DOG/go-sqlmock"
)

func TestInsertCandleData(t *testing.T) {
	type args struct {
		data     [][]interface{}
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
				data: [][]interface{}{
					{
						1620990000000.0, "60000", "61000", "59000", "60500", "1000",
						1620993599999.0, "60000000", "500", "0", "0", "0",
					},
				},
				interval: "1m",
				symbol:   "BTCUSDT",
			},
			mockSetup: func(mock sqlmock.Sqlmock) {
				// SELECT query
				mock.ExpectQuery(regexp.QuoteMeta("SELECT pair_id FROM pair WHERE symbol = $1")).
					WithArgs("BTCUSDT").
					WillReturnRows(sqlmock.NewRows([]string{"pair_id"}).AddRow(1))

				// INSERT
				mock.ExpectBegin()
				mock.ExpectPrepare(regexp.QuoteMeta(
					"INSERT INTO candle ( pair_id, open_time, open, high, low, close, volume, close_time, quote_volume, trades, taker_base_vol, taker_quote_vol, timeframe ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)",
				)).ExpectExec().
					WithArgs(
						1,
						time.UnixMilli(1620990000000.0).Format("2006-01-02 15:04:05"), "60000", "61000", "59000", "60500",
						"1000", time.UnixMilli(1620993599999.0).Format("2006-01-02 15:04:05"), "60000000", "500", "0", "0", "1m",
					).
					WillReturnResult(sqlmock.NewResult(1, 1))
				mock.ExpectCommit()
			},

			wantErr: false,
		},
		{
			name: "fail on insert exec",
			args: args{
				data: [][]interface{}{
					{
						1620990000000.0, "60000", "61000", "59000", "60500", "1000",
						1620993599999.0, "60000000", "500", "0", "0", "0",
					},
				},
				interval: "1m",
				symbol:   "BTCUSDT",
			},
			mockSetup: func(mock sqlmock.Sqlmock) {
				// getPairId query
				mock.ExpectQuery(regexp.QuoteMeta("SELECT pair_id FROM pair WHERE symbol = $1")).
					WithArgs("BTCUSDT").
					WillReturnRows(sqlmock.NewRows([]string{"pair_id"}).AddRow(1))

				mock.ExpectBegin()
				mock.ExpectPrepare(regexp.QuoteMeta(
					"INSERT INTO candle ( pair_id, open_time, open, high, low, close, volume, close_time, quote_volume, trades, taker_base_vol, taker_quote_vol, timeframe ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)",
				)).ExpectExec().
					WithArgs(sqlmock.AnyArg(), sqlmock.AnyArg(), sqlmock.AnyArg(),
						sqlmock.AnyArg(), sqlmock.AnyArg(), sqlmock.AnyArg(),
						sqlmock.AnyArg(), sqlmock.AnyArg(), sqlmock.AnyArg(),
						sqlmock.AnyArg(), sqlmock.AnyArg(), sqlmock.AnyArg(), sqlmock.AnyArg()).
					WillReturnError(errors.New("insert failed"))
				mock.ExpectRollback()
			},
			wantErr: true,
		},
		{
			name: "fail on finding pair",
			args: args{
				data: [][]interface{}{
					{
						1620990000000.0, "60000", "61000", "59000", "60500", "1000",
						1620993599999.0, "60000000", "500", "0", "0", "0",
					},
				},
				interval: "1m",
				symbol:   "XYZ",
			},
			mockSetup: func(mock sqlmock.Sqlmock) {
				// getPairId query
				mock.ExpectQuery(regexp.QuoteMeta("SELECT pair_id FROM pair WHERE symbol = $1")).
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

			err = insertCandleData(db, tt.args.data, tt.args.interval, tt.args.symbol)
			if (err != nil) != tt.wantErr {
				t.Errorf("insertCandleData() error = %v, wantErr %v", err, tt.wantErr)
			}

			if err := mock.ExpectationsWereMet(); err != nil {
				t.Errorf("unfulfilled expectations: %v", err)
			}
		})
	}
}
