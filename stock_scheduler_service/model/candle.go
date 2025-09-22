package model

type Candle struct {
	OpenTime      string `json:"open_time"`
	Open          string `json:"open"`
	High          string `json:"high"`
	Low           string `json:"low"`
	Close         string `json:"close"`
	Volume        string `json:"volume"`
	CloseTime     string `json:"close_time"`
	QuoteVolume   string `json:"quote_volume"`
	Trades        string `json:"trades"`
	TakerBaseVol  string `json:"taker_base_vol"`
	TakerQuoteVol string `json:"taker_quote_vol"`
	Ignore        string `json:"ignore"`
}
