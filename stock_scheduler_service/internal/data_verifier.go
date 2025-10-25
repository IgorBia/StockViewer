// package internal
// /*
// Data Integration verification:
// 	For every symbol and timeframe:
// 		1. Get the oldest candle
// 		2. Get the next candle if not in future
// 		3. If missing fetch the candle 
// 		4. Repeat step 2 until next candle in future
// */
// func EnsureDataIntegrity(db *sql.DB){
// 	SupportedSymbols := []string{"ETHUSDC", "BTCUSDC", "SOLUSDC", "ETHBTC"}

// 	for _, symbol := range SupportedSymbols {
// 		go EnsureDataIntegrityForSymbolAndInterval(db, symbol, "1s", 1*time.Second)
// 		go EnsureDataIntegrityForSymbolAndInterval(db, symbol, "1h", 1*time.Hour)
// 		go EnsureDataIntegrityForSymbolAndInterval(db, symbol, "4h", 4*time.Hour)
// 		go EnsureDataIntegrityForSymbolAndInterval(db, symbol, "1d", 24*time.Hour)
// 	}
// }
// func EnsureDataIntegrityForSymbolAndInterval(db *sql.DB, symbol string, interval string, duration time.Duration){
// 	actualCandle := GetTheOldestCandle()
// 	if actualCandle==nil{
// 		return nil; 	// db is empty therefore integral
// 	}
// 	for true {
// 		nextOpenTime := CalculateNextOpenTime(actualCandle)
// 		if err{
// 			return // db is integral = error opentime is in future - oldest to now checked
// 		}
// 		actualCandle = getNextCandle(db, symbol, interval, nextOpenTime)
// 		if actualCandle==nil && err!=nil {
// 			patchCandle(missingCandleTime)
// 		}
// 	}
// }
// func getNextCandle(){}
// 	//get opentime from current and calculate next opentime then select from db the next candle
// func patchCandle(){
// 	// call for api with next candle open time
// }