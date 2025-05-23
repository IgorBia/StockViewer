package com.stockviewer.stockapi.mapper;

import com.stockviewer.stockapi.dto.CandleDTO;
import com.stockviewer.stockapi.entity.Candle;
import org.springframework.stereotype.Component;

@Component
public class CandleMapper {

    public CandleDTO toDTO(Candle candle) {
        if (candle == null) {
            throw new IllegalArgumentException("Candle cannot be null");
        }
        return new CandleDTO(
                candle.getOpenTime(),
                candle.getOpen(),
                candle.getClose(),
                candle.getHigh(),
                candle.getLow()
        );
    }
}
