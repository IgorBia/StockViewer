package com.stockviewer.stockapi.indicators;

import com.stockviewer.stockapi.candle.entity.Candle;
import com.stockviewer.stockapi.candle.entity.Pair;
import com.stockviewer.stockapi.candle.entity.Indicator;
import com.stockviewer.stockapi.candle.service.CandleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndicatorCalculatorTest {

    @Mock
    CandleService candleService;

    @InjectMocks
    IndicatorCalculator calculator;

    private List<Candle> buildFlatCandles(String symbol, String timeframe, int count, String price) {
        List<Candle> candles = new ArrayList<>();
        Pair pair = org.mockito.Mockito.mock(Pair.class);
        when(pair.getSymbol()).thenReturn(symbol);

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < count; i++) {
            Candle c = new Candle();
            c.setPair(pair);
            c.setTimeframe(timeframe);
            c.setTimestamp(now.minusMinutes(count - i));
            c.setCloseTime(now.minusMinutes(count - i - 1));
            c.setOpen(new BigDecimal(price));
            c.setHigh(new BigDecimal(price));
            c.setLow(new BigDecimal(price));
            c.setClose(new BigDecimal(price));
            c.setVolume(new BigDecimal("0"));
            candles.add(c);
        }
        return candles;
    }

    @Test
    void calculateIndicators_flatSeries_producesExpectedValues() {
        // given: flat price series (all closes = 100) length >= 30 to satisfy MACD window
        String symbol = "TESTPAIR";
        String timeframe = "1m";
        List<Candle> series = buildFlatCandles(symbol, timeframe, 30, "100");

        when(candleService.getCandlesBySymbolAndTimeframe(symbol, timeframe)).thenReturn(series);

        Candle sample = series.get(series.size() - 1);

        // when
        Indicator ema9 = calculator.calculateEMA9(sample);
        Indicator macd = calculator.calculateMACD(sample);
        Indicator macdSignal = calculator.calculateMACDSignal(sample);

        // then
        assertNotNull(ema9);
        assertNotNull(macd);
        assertNotNull(macdSignal);

        // EMA of flat series should equal the constant price
        assertEquals(0, ema9.getValue().compareTo(new BigDecimal("100")));

        // MACD (difference between EMAs) on flat series should be zero
        assertEquals(0, macd.getValue().compareTo(BigDecimal.ZERO));
        assertEquals(0, macdSignal.getValue().compareTo(BigDecimal.ZERO));
    }
}
