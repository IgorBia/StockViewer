package com.stockviewer.stockapi.indicators;

import com.stockviewer.stockapi.candle.entity.Candle;
import com.stockviewer.stockapi.candle.entity.Indicator;
import com.stockviewer.stockapi.candle.service.CandleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class IndicatorCalculator {

    private final CandleService candleService;
    private final Logger logger = LoggerFactory.getLogger(IndicatorCalculator.class);

    public IndicatorCalculator(CandleService candleService) {
        this.candleService = candleService;
    }

    public Indicator calculateMACD(Candle candle) {
        return new Indicator("MACD", candle, calculateMACDValue(candle), LocalDateTime.now());
    }

    private BigDecimal calculateMACDValue(Candle candle) {
        BarSeries series = getBarSeries("macd_series", candle);
        if(series.getBarCount()<26) return BigDecimal.ZERO;

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);

        return macd.getValue(series.getEndIndex()).bigDecimalValue();
    }

    public Indicator calculateEMA9(Candle candle) {
        return new Indicator("EMA9", candle, calculateEMA9Value(candle), LocalDateTime.now());
    }

    private BigDecimal calculateEMA9Value(Candle candle) {
        BarSeries series = getBarSeries("EMA9_series", candle);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator ema9 = new EMAIndicator(closePrice, 9);

        return ema9.getValue(series.getEndIndex()).bigDecimalValue();
    }

    public Indicator calculateMACDSignal(Candle candle) {
        return new Indicator("MACD_SIGNAL", candle, calculateMACDSignalValue(candle), LocalDateTime.now());
    }

    private BigDecimal calculateMACDSignalValue(Candle candle) {
        BarSeries series = getBarSeries("macd_signal_series", candle);
        if(series.getBarCount() < 26) return BigDecimal.ZERO;

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        
        EMAIndicator signal = new EMAIndicator(macd, 9);

        return signal.getValue(series.getEndIndex()).bigDecimalValue();
    }

    private BarSeries getBarSeries(String seriesName, Candle candle){
        List<Candle> candles = candleService.getCandlesBySymbolAndTimeframe(candle.getPair().getSymbol(), candle.getTimeframe());

        BarSeries series = new BaseBarSeries(seriesName);

        for (Candle c : candles) {
            Bar bar = BaseBar.builder()
                    .timePeriod(java.time.Duration.ofMinutes(TimeframeParser.parseTimeframe(c.getTimeframe())))
                    .endTime(c.getCloseTime().atZone(ZoneId.systemDefault())) // albo LocalDateTime → atZone()
                    .openPrice(DecimalNum.valueOf(c.getOpen()))
                    .highPrice(DecimalNum.valueOf(c.getHigh()))
                    .lowPrice(DecimalNum.valueOf(c.getLow()))
                    .closePrice(DecimalNum.valueOf(c.getClose()))
                    .volume(DecimalNum.valueOf(c.getVolume()))
                    .build();
            series.addBar(bar);
        }

        return series;
    }
}
