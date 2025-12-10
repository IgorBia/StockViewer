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
import com.stockviewer.stockapi.user.service.UserService;
import com.stockviewer.stockapi.wallet.service.WalletService;

import org.springframework.stereotype.Service;

@Service
public class TradeService {
    
    private final WalletService walletService;
    private final UserService userService;
    private final CandleService candleService;
    private final TradeRepository tradeRepository;
    private final TradeMapper tradeMapper;

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
        Wallet wallet = walletService.getWallet(user);
        AmountsDTO amountsDTO = calculateExchangeAmounts(pairSymbol, amount, transactionType);
        Pair pair = candleService.getPair(pairSymbol);
        if(!validateTrade(wallet, amountsDTO, transactionType, pair)) {
            throw new IllegalArgumentException("Insufficient assets for the transaction");
        }

        Trade trade = new Trade();
        trade.setUser(user);
        trade.setPair(pair);
        trade.setTimestamp(java.time.LocalDateTime.now());
        trade.setPrice(candleService.getTicker(pairSymbol));
        trade.setBaseAmount(amountsDTO.baseAmount());
        trade.setQuoteAmount(amountsDTO.quoteAmount());
        
        if (transactionType == TransactionType.BUY) {
            trade.setTransactionType("BUY");
            walletService.updateOwnedAsset(wallet, pair.getBaseAsset().getSymbol(), amountsDTO.baseAmount());
            walletService.updateOwnedAsset(wallet, pair.getQuoteAsset().getSymbol(), amountsDTO.quoteAmount().negate());
        } else if (transactionType == TransactionType.SELL) {
            trade.setTransactionType("SELL");   
            walletService.updateOwnedAsset(wallet, pair.getBaseAsset().getSymbol(), amountsDTO.baseAmount().negate());
            walletService.updateOwnedAsset(wallet, pair.getQuoteAsset().getSymbol(), amountsDTO.quoteAmount());
        }

        tradeRepository.save(trade);

        return walletService.getOwnedAssetsByUser(); 
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
        return wallet.getOwnedAssets().stream()
                .filter(owned -> owned.getAsset().getSymbol().equals(assetSymbol))
                .findFirst()
                .map(owned -> owned.getAmount().compareTo(requiredAmount) >= 0)
                .orElse(false);
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
}
