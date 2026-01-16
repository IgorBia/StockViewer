package com.stockviewer.stockapi.trade.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import com.stockviewer.stockapi.wallet.entity.Asset;
import com.stockviewer.stockapi.candle.entity.Pair;
import com.stockviewer.stockapi.candle.service.CandleService;
import com.stockviewer.stockapi.trade.dto.TransactionType;
import com.stockviewer.stockapi.trade.mapper.TradeMapper;
import com.stockviewer.stockapi.trade.repository.TradeRepository;
import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.user.service.UserService;
import com.stockviewer.stockapi.wallet.entity.Wallet;
import com.stockviewer.stockapi.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.lenient;
import com.stockviewer.stockapi.exception.InsufficientFundsException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TradeServiceTest {

    @Mock
    private WalletService walletService;

    @Mock
    private UserService userService;

    @Mock
    private CandleService candleService;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeMapper tradeMapper;

    @InjectMocks
    private TradeService tradeService;

    @Test
    void executeTradeForUser_throwsWhenInsufficientFundsForBuy() {
        // Arrange
        User user = mock(User.class);
        Wallet wallet = mock(Wallet.class);

        Asset baseAsset = mock(Asset.class);
        Asset quoteAsset = mock(Asset.class);
        Pair pair = mock(Pair.class);

        // walletService returns wallet for user
        when(walletService.getWallet(user)).thenReturn(wallet);
        // wallet has zero of quote asset (e.g. USDC) -> insufficient for BUY
        when(walletService.getOwnedAssetAmountForWallet(wallet, "USDC")).thenReturn(BigDecimal.ZERO);

        // prepare pair metadata: base BTC, quote USDC
        when(candleService.getPair("BTCUSDC")).thenReturn(pair);
        when(candleService.getTicker("BTCUSDC")).thenReturn(new BigDecimal("100"));
        when(pair.getQuoteAsset()).thenReturn(quoteAsset);
        when(quoteAsset.getSymbol()).thenReturn("USDC");
        when(pair.getBaseAsset()).thenReturn(baseAsset);
        when(baseAsset.getSymbol()).thenReturn("BTC");

        // Act & Assert: BUY should fail validation -> IllegalArgumentException
        assertThrows(InsufficientFundsException.class, () -> {
            tradeService.executeTradeForUser(user, "BTCUSDC", new BigDecimal("100"), TransactionType.BUY);
        });
    }
}