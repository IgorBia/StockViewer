package com.stockviewer.stockapi.trade.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

import com.stockviewer.stockapi.candle.entity.Pair;
import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.wallet.dto.AmountsDTO;
import com.stockviewer.stockapi.wallet.dto.OwnedAssetDTO;
import com.stockviewer.stockapi.wallet.entity.Wallet;
import com.stockviewer.stockapi.candle.service.CandleService;
import com.stockviewer.stockapi.exception.ResourceNotFoundException;
import com.stockviewer.stockapi.trade.dto.TradeDTO;
import com.stockviewer.stockapi.trade.dto.TransactionType;
import com.stockviewer.stockapi.trade.entity.Trade;
import com.stockviewer.stockapi.trade.mapper.TradeMapper;
import com.stockviewer.stockapi.trade.repository.TradeRepository;
import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.user.service.UserService;
import com.stockviewer.stockapi.wallet.service.WalletService;
import com.stockviewer.stockapi.wallet.entity.OwnedAsset;
import com.stockviewer.stockapi.candle.dto.CandleDTO;
import com.stockviewer.stockapi.candle.entity.Candle;
import com.stockviewer.stockapi.indicators.IndicatorDTO;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.stockviewer.stockapi.exception.InsufficientFundsException;
import org.springframework.stereotype.Service;

@Service
public class TradeService {
    
    private final WalletService walletService;
    private final UserService userService;
    private final CandleService candleService;
    private final TradeRepository tradeRepository;
    private final TradeMapper tradeMapper;
    private final Logger logger = LoggerFactory.getLogger(TradeService.class);

    public TradeService(WalletService walletService, UserService userService, CandleService candleService, TradeRepository tradeRepository, TradeMapper tradeMapper) {
        this.walletService = walletService;
        this.userService = userService;
        this.candleService = candleService;
        this.tradeRepository = tradeRepository;
        this.tradeMapper = tradeMapper;
    }

    public List<TradeDTO> getTradeHistoryForUser() {
        User user = userService.getUserFromContext();
        return tradeRepository.findAllByUser(user)
            .map(tradeMapper::toDTOList)
            .orElseThrow(() -> new ResourceNotFoundException("No trades found for user"));
    }

    public Set<OwnedAssetDTO> executeTrade(String pairSymbol, BigDecimal amount, TransactionType transactionType) {
        User user = userService.getUserFromContext();
        return executeTradeForUser(user, pairSymbol, amount, transactionType);
    }

    public Set<OwnedAssetDTO> executeTradeForUser(User user, String pairSymbol, BigDecimal amount, TransactionType transactionType) {
        Trade trade = buildTrade(user, pairSymbol, amount, transactionType);
        return executeTradeForUser(user, trade, transactionType);
    }

    public Set<OwnedAssetDTO> executeTradeForUser(User user, String pairSymbol, BigDecimal amount, TransactionType transactionType, BigDecimal stopLoss, BigDecimal takeProfit) {

        Trade trade = buildTrade(user, pairSymbol, amount, transactionType);
        trade.setStopLoss(stopLoss);
        trade.setTakeProfit(takeProfit);
        
        return executeTradeForUser(user, trade, transactionType);

    }

    private Trade buildTrade(User user, String pairSymbol, BigDecimal amount, TransactionType transactionType) {
        Wallet wallet = walletService.getWallet(user);
        AmountsDTO amountsDTO = calculateExchangeAmounts(pairSymbol, amount, transactionType);
        Pair pair = candleService.getPair(pairSymbol);

        Trade trade = new Trade();
        trade.setUser(user);
        trade.setPair(pair);
        trade.setTimestamp(java.time.LocalDateTime.now());
        trade.setPrice(candleService.getTicker(pairSymbol));
        trade.setBaseAmount(amountsDTO.baseAmount());
        trade.setQuoteAmount(amountsDTO.quoteAmount());

        return trade;
    }

    private Set<OwnedAssetDTO> executeTradeForUser(User user, Trade trade, TransactionType transactionType) {
        Wallet wallet = walletService.getWallet(user);
        AmountsDTO amountsDTO = new AmountsDTO(trade.getBaseAmount(), trade.getQuoteAmount());
        Pair pair = trade.getPair();
        if(!validateTrade(wallet, amountsDTO, transactionType, pair)) {
            throw new InsufficientFundsException("Insufficient assets for the transaction");
        }
        if (transactionType == TransactionType.BUY) {
            trade.setTransactionType("BUY");
            walletService.updateOwnedAsset(wallet, trade.getPair().getBaseAsset().getSymbol(), trade.getBaseAmount(), candleService.getTicker(trade.getPair().getSymbol()));
            walletService.updateOwnedAsset(wallet, trade.getPair().getQuoteAsset().getSymbol(), trade.getQuoteAmount().negate());
        } else if (transactionType == TransactionType.SELL) {
            trade.setTransactionType("SELL");
            trade.setPnl(calculatePnLForSellTrade(wallet, trade));
            walletService.updateOwnedAsset(wallet, trade.getPair().getBaseAsset().getSymbol(), trade.getBaseAmount().negate());
            walletService.updateOwnedAsset(wallet, trade.getPair().getQuoteAsset().getSymbol(), trade.getQuoteAmount());
        }

        logger.info("Executing {} trade for user {} on pair {}: baseAmount={}, quoteAmount={}",
            transactionType, user.getEmail(), trade.getPair().getSymbol(), trade.getBaseAmount(), trade.getQuoteAmount());
        tradeRepository.save(trade);

        return walletService.getOwnedAssetsByUser(user); 
    }

    private boolean validateTrade(Wallet wallet, AmountsDTO amountsDTO, TransactionType transactionType, Pair pair) {
        if(transactionType == TransactionType.BUY) {
            return validateSufficientAssets(wallet, pair.getQuoteAsset().getSymbol(), amountsDTO.quoteAmount());
        } else if (transactionType == TransactionType.SELL) {
            return validateSufficientAssets(wallet, pair.getBaseAsset().getSymbol(), amountsDTO.baseAmount());
        }
        return false;
    }

private boolean validateSufficientAssets(Wallet wallet, String assetSymbol, BigDecimal requiredAmount) {
    if (requiredAmount == null) {
        throw new IllegalArgumentException("requiredAmount must not be null");
    }
    if (requiredAmount.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("requiredAmount must be non-negative");
    }

    if (wallet == null) {
        logger.debug("Wallet is null while validating asset availability for symbol {}", assetSymbol);
        return false;
    }

    BigDecimal available = walletService.getOwnedAssetAmountForWallet(wallet, assetSymbol);
    if (available == null) {
        available = BigDecimal.ZERO;
    }

    boolean sufficient = available.compareTo(requiredAmount) >= 0;
    if (!sufficient) {
        logger.debug("Insufficient assets for wallet {} asset {}: available={}, required={}",
            wallet.getId() != null ? wallet.getId() : "<unknown>", assetSymbol, available, requiredAmount);
    }
    return sufficient;
}

    private AmountsDTO calculateExchangeAmounts(String pairSymbol, BigDecimal amount, TransactionType transactionType) {
        BigDecimal pairPrice = candleService.getTicker(pairSymbol);

        BigDecimal baseAmount;
        BigDecimal quoteAmount;
        final int SCALE = 8;

        if(transactionType == TransactionType.BUY) {
            quoteAmount = amount;
            baseAmount = amount.divide(pairPrice, SCALE, RoundingMode.HALF_UP);
        } else if (transactionType == TransactionType.SELL) {
            baseAmount = amount;
            quoteAmount = amount.multiply(pairPrice);
        } else {
            throw new IllegalArgumentException("Invalid transaction type");
        }

        return new AmountsDTO(baseAmount, quoteAmount);
    }
    

    public void executeTradeForManagedAssets(Pair pair) {
        List<User> usersWithManagedAssets = userService.getUsersWithManagedAssetsInPair(pair);
        logger.info("Executing trades for managed assets for pair {} (users={})", pair.getSymbol(), usersWithManagedAssets.size());

        BigDecimal price = candleService.getTicker(pair.getSymbol());

        // Ograniczenie równoległości - dopasuj liczbę wątków do środowiska (DB/API)
        final int maxWorkers = Math.min(8, Math.max(1, usersWithManagedAssets.size()));
        ExecutorService exec = Executors.newFixedThreadPool(maxWorkers);

        for (User user : usersWithManagedAssets) {
            exec.submit(() -> {
                try {
                    processManagedAssetForUser(user, pair, price);
                } catch (Exception ex) {
                    logger.error("Error processing managed asset trades for user {}: {}", user.getEmail(), ex.getMessage(), ex);
                }
            });
        }

        // opcjonalnie czekać na zakończenie zadań; jeśli chcesz fire-and-forget, pomiń poniższy blok
        exec.shutdown();
        try {
            if (!exec.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn("Timeout waiting for managed-asset tasks to finish; forcing shutdown");
                exec.shutdownNow();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            exec.shutdownNow();
        }
    }

    private void processManagedAssetForUser(User user, Pair pair, BigDecimal price) {
        logger.debug("Checking user {} for managed asset trades...", user.getEmail());

        Wallet wallet = walletService.getWallet(user);
        BigDecimal walletQuoteAmount = walletService.getOwnedAssetAmountForWallet(wallet, pair.getQuoteAsset().getSymbol());
        BigDecimal walletBaseAmount = walletService.getOwnedAssetAmountForWallet(wallet, pair.getBaseAsset().getSymbol());
        BigDecimal totalValue = walletBaseAmount.multiply(price).add(walletQuoteAmount);

        logger.info("User {} wallet for pair {}: baseAmount={}, quoteAmount={}, totalValue={}",
                user.getEmail(), pair.getSymbol(), walletBaseAmount, walletQuoteAmount, totalValue);

        final BigDecimal twoPercentFactor = BigDecimal.valueOf(50); // denominator for 2%

        if (buySignalForPair(pair.getSymbol())) {
            if (walletQuoteAmount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.debug("No quote amount for user {}; skipping BUY", user.getEmail());
                return;
            }
            BigDecimal targetQuote = totalValue.divide(twoPercentFactor, 8, RoundingMode.HALF_UP); // 2% of total value in quote
            BigDecimal baseToBuy = targetQuote.divide(price, 8, RoundingMode.HALF_UP);
            if (baseToBuy.compareTo(BigDecimal.ZERO) <= 0) {
                logger.debug("Computed baseToBuy is zero for user {}; skipping", user.getEmail());
                return;
            }
            logger.info("Executing BUY trade for user {} on pair {} baseAmount={}", user.getEmail(), pair.getSymbol(), baseToBuy);
            // build Trade using known price to avoid an extra ticker fetch
            Trade trade = buildTradeWithPrice(user, pair, baseToBuy, TransactionType.BUY, price);
            trade.setStopLoss(price.multiply(BigDecimal.valueOf(0.998)));
            trade.setTakeProfit(price.multiply(BigDecimal.valueOf(1.002)));
            executeTradeForUser(user, trade, TransactionType.BUY);
        } else if (sellSignalForPair(pair.getSymbol())) {
            if (walletBaseAmount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.debug("No base amount for user {}; skipping SELL", user.getEmail());
                return;
            }
            BigDecimal targetQuote = totalValue.divide(twoPercentFactor, 8, RoundingMode.HALF_UP);
            BigDecimal targetBase = targetQuote.divide(price, 8, RoundingMode.HALF_UP); // desired base to sell
            BigDecimal sellBase = targetBase.min(walletBaseAmount);

            if (sellBase.compareTo(BigDecimal.ZERO) <= 0) {
                logger.debug("Computed sellBase is zero for user {}; skipping", user.getEmail());
                return;
            }
            logger.info("Executing SELL trade for user {} on pair {} baseAmount={}", user.getEmail(), pair.getSymbol(), sellBase);
            Trade trade = buildTradeWithPrice(user, pair, sellBase, TransactionType.SELL, price);
            executeTradeForUser(user, trade, TransactionType.SELL);
        }
    }

    // helper: build Trade using supplied price (no extra ticker call)
    private Trade buildTradeWithPrice(User user, Pair pair, BigDecimal baseAmount, TransactionType txType, BigDecimal price) {
        Wallet wallet = walletService.getWallet(user); // to validate and ensure wallet exists
        Trade trade = new Trade();
        trade.setUser(user);
        trade.setPair(pair);
        trade.setTimestamp(java.time.LocalDateTime.now());
        trade.setPrice(price);
        trade.setBaseAmount(baseAmount);

        // quoteAmount = baseAmount * price (for SELL), or computed for BUY in earlier code we pass baseAmount already
        if (txType == TransactionType.SELL) {
            trade.setQuoteAmount(baseAmount.multiply(price));
        } else {
            // for BUY we already computed baseAmount as amount to buy, compute quote as base*price
            trade.setQuoteAmount(baseAmount.multiply(price));
        }

        return trade;
    }
    

    private boolean buySignalForPair(String pairSymbol) {
        List<CandleDTO> candles = candleService.getCandlesDTOBySymbolAndTimeframe(pairSymbol, "15m");
        if (candles == null || candles.size() < 2) {
            logger.warn("Not enough candles for pair {} to evaluate buy signal (need >=2)", pairSymbol);
            return false;
        }
        CandleDTO lastCandle = candles.get(candles.size() - 1);
        CandleDTO prevCandle = candles.get(candles.size() - 2);
        java.math.BigDecimal lastMacd = getMACDValue(lastCandle);
        java.math.BigDecimal lastSignal = getMACDSignalValue(lastCandle);
        java.math.BigDecimal prevMacd = getMACDValue(prevCandle);
        java.math.BigDecimal prevSignal = getMACDSignalValue(prevCandle);

        if (lastMacd == null || lastSignal == null || prevMacd == null || prevSignal == null) {
            logger.warn("Missing MACD data for pair {} — skipping buy evaluation", pairSymbol);
            return false;
        }
        logger.debug("Evaluating buy signal for pair {}: lastMacd={}, lastSignal={}, prevMacd={}, prevSignal={}",
                pairSymbol, lastMacd, lastSignal, prevMacd, prevSignal);
        if (lastMacd.compareTo(lastSignal) > 0) {
            return true;
        }
        return false;
    }

    private boolean sellSignalForPair(String pairSymbol) {
        List<CandleDTO> candles = candleService.getCandlesDTOBySymbolAndTimeframe(pairSymbol, "15m");
        if (candles == null || candles.size() < 2) {
            logger.warn("Not enough candles for pair {} to evaluate sell signal (need >=2)", pairSymbol);
            return false;
        }
        CandleDTO lastCandle = candles.get(candles.size() - 1);
        CandleDTO prevCandle = candles.get(candles.size() - 2);
        java.math.BigDecimal lastMacd = getMACDValue(lastCandle);
        java.math.BigDecimal lastSignal = getMACDSignalValue(lastCandle);
        java.math.BigDecimal prevMacd = getMACDValue(prevCandle);
        java.math.BigDecimal prevSignal = getMACDSignalValue(prevCandle);

        if (lastMacd == null || lastSignal == null || prevMacd == null || prevSignal == null) {
            logger.warn("Missing MACD data for pair {} — skipping sell evaluation", pairSymbol);
            return false;
        }

        logger.debug("Evaluating sell signal for pair {}: lastMacd={}, lastSignal={}, prevMacd={}, prevSignal={}",
                pairSymbol, lastMacd, lastSignal, prevMacd, prevSignal);
        if (lastMacd.compareTo(lastSignal) < 0) {
            return true;
        }
        return false;
    }

    private BigDecimal getMACDValue(CandleDTO candle){
        List<IndicatorDTO> indicators = candle.indicators();
        if (indicators == null || indicators.isEmpty()) {
            logger.warn("Candle has no indicators: timestamp={}, closeTime={}", candle.timestamp(), candle.closeTime());
            return null;
        }
        return indicators.stream()
            .filter(indicator -> "MACD".equalsIgnoreCase(indicator.name()))
            .findFirst()
            .map(IndicatorDTO::value)
            .orElseGet(() -> {
                logger.warn("MACD indicator not found for candle: timestamp={}, closeTime={}", candle.timestamp(), candle.closeTime());
                return null;
            });
    }

    private BigDecimal getMACDSignalValue(CandleDTO candle){
        List<IndicatorDTO> indicators = candle.indicators();
        if (indicators == null || indicators.isEmpty()) {
            logger.warn("Candle has no indicators: timestamp={}, closeTime={}", candle.timestamp(), candle.closeTime());
            return null;
        }
        return indicators.stream()
            .filter(indicator -> "MACD_SIGNAL".equalsIgnoreCase(indicator.name()) || "SIGNAL".equalsIgnoreCase(indicator.name()))
            .findFirst()
            .map(IndicatorDTO::value)
            .orElseGet(() -> {
                logger.warn("MACD_SIGNAL indicator not found for candle: timestamp={}, closeTime={}", candle.timestamp(), candle.closeTime());
                return null;
            });
    }

    private BigDecimal getOwnedAssetAmountFromWallet(Wallet wallet, String assetSymbol) {
        if (wallet == null) {
            return BigDecimal.ZERO;
        }
        return walletService.getOwnedAssetAmountForWallet(wallet, assetSymbol);
    }

    private BigDecimal calculatePnLForSellTrade(Wallet wallet, Trade trade) {
        String baseAssetSymbol = trade.getPair().getBaseAsset().getSymbol();
        BigDecimal ownedAmount = walletService.getOwnedAssetAmountForWallet(wallet, baseAssetSymbol);
        
        if (ownedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("No owned amount for asset {} in wallet when calculating PnL", baseAssetSymbol);
            return BigDecimal.ZERO;
        }

        OwnedAsset owned = walletService.getOwnedAssetEntityForWallet(wallet, baseAssetSymbol);
        BigDecimal avgPurchasePrice = (owned != null && owned.getAvgPrice() != null) ? owned.getAvgPrice() : BigDecimal.ZERO;
        BigDecimal sellPrice = trade.getPrice();
        BigDecimal baseAmountSold = trade.getBaseAmount();
        BigDecimal pnl = sellPrice.subtract(avgPurchasePrice).multiply(baseAmountSold);

        logger.info("Calculated PnL for SELL trade: asset={}, soldAmount={}, avgPurchasePrice={}, sellPrice={}, PnL={}",
                baseAssetSymbol, baseAmountSold, avgPurchasePrice, sellPrice, pnl);

        return pnl;
    }

    public void executeStopLossTakeProfit(Pair pair, Candle candle) {
        List<User> users = userService.getAllUsers();
        logger.info("Executing Stop Loss / Take Profit for pair {} on candle {}", pair.getSymbol(), candle.getTimestamp());
        for (User user : users) {
            // get trades that have stop loss or take profit set for this pair
            List<Trade> trades = tradeRepository.findAllWithStopOrTakeForUserAndPair(user, pair);
            logger.info("Found {} trades with Stop Loss/Take Profit for user {} on pair {}", trades.size(), user.getEmail(), pair.getSymbol());
            for (Trade trade : trades) {
                BigDecimal candleLow = candle.getLow();
                BigDecimal candleHigh = candle.getHigh();
                boolean executed = false;

                String baseAssetSymbol = candleService.findBaseAssetSymbolById(pair.getId());
                Wallet wallet = walletService.getWallet(user);
                BigDecimal ownedAmount = walletService.getOwnedAssetAmountForWallet(wallet, baseAssetSymbol);

                try {
                    if (trade.getStopLoss() != null && candleLow.compareTo(trade.getStopLoss()) <= 0) {
                        if (ownedAmount.compareTo(trade.getBaseAmount()) < 0) {
                            logger.warn("Skipping Stop Loss for user {} on trade {}: insufficient owned amount (owned={}, needed={})",
                                    user.getEmail(), trade.getTradeId(), ownedAmount, trade.getBaseAmount());
                        } else {
                            logger.info("Executing Stop Loss for user {} on trade {}", user.getEmail(), trade.getTradeId());
                            executeTradeForUser(user, pair.getSymbol(), trade.getBaseAmount(), TransactionType.SELL);
                            executed = true;
                        }
                    } else if (trade.getTakeProfit() != null && candleHigh.compareTo(trade.getTakeProfit()) >= 0) {
                        if (ownedAmount.compareTo(trade.getBaseAmount()) < 0) {
                            logger.warn("Skipping Take Profit for user {} on trade {}: insufficient owned amount (owned={}, needed={})",
                                    user.getEmail(), trade.getTradeId(), ownedAmount, trade.getBaseAmount());
                        } else {
                            logger.info("Executing Take Profit for user {} on trade {}", user.getEmail(), trade.getTradeId());
                            executeTradeForUser(user, pair.getSymbol(), trade.getBaseAmount(), TransactionType.SELL);
                            executed = true;
                        }
                    }

                    if (executed) {
                        logger.info("Trade {} for user {} executed due to Stop Loss/Take Profit", trade.getTradeId(), user.getEmail());
                        // clear stop loss and take profit after execution
                        trade.setStopLoss(null);
                        trade.setTakeProfit(null);
                        tradeRepository.save(trade);
                    }
                } catch (IllegalArgumentException iae) {
                    logger.warn("Skipping execution for trade {} user {} due to validation: {}", trade.getTradeId(), user.getEmail(), iae.getMessage());
                } catch (Exception ex) {
                    logger.error("Error executing StopLoss/TakeProfit for user {} trade {}: {}", user.getEmail(), trade.getTradeId(), ex.getMessage(), ex);
                }
            }
        }
    }
}
