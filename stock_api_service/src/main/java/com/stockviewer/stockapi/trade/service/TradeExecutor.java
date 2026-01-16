// service scheduled to run every minute to check for stop loss / take profit execution and manage trades with managed assets
package com.stockviewer.stockapi.trade.service;

import com.stockviewer.stockapi.candle.service.CandleConfig;
import com.stockviewer.stockapi.candle.entity.Candle;
import com.stockviewer.stockapi.candle.entity.Pair;
import com.stockviewer.stockapi.candle.service.CandleService;
import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.wallet.entity.Wallet;
import com.stockviewer.stockapi.wallet.service.WalletService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service; 

@Service
public class TradeExecutor {
    private final TradeService tradeService;
    private final CandleService candleService;
    private final CandleConfig candleConfig;
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TradeExecutor.class);

    public TradeExecutor(TradeService tradeService, CandleService candleService, CandleConfig candleConfig) {
        this.tradeService = tradeService;
        this.candleService = candleService;
        this.candleConfig = candleConfig;
    }

    // @Scheduled(cron = "15 * * * * *")
    // public void checkStopLossTakeProfit() {
    //     for (Pair pair : candleService.getAllPairs()) {
    //         Candle candle = candleService.getLatestCandleForPair(pair, "1m");
    //         if (candle != null) {
    //             tradeService.executeStopLossTakeProfit(pair, candle);
    //         } else {
    //             logger.warn("No latest candle found for pair {}", pair.getSymbol());
    //         }
    //     }
    // }

    // this method has to iterate over all pairs
    @Scheduled(cron = "30 * * * * *") // Every minute at second 30
    public void executeTradesForManagedAssets() {
        for (Pair pair : candleService.getAllPairs()) {
            logger.info("[" + pair.getSymbol() + "] Executing trades for managed accounts...");
            tradeService.executeTradeForManagedAssets(pair);
        }
    }
}