package com.stockviewer.stockapi.candle.mapper;

import com.stockviewer.stockapi.candle.dto.CandleDTO;
import com.stockviewer.stockapi.candle.entity.Candle;
import org.mapstruct.Mapper;
import org.mapstruct.BeforeMapping;

@Mapper(componentModel = "spring")
public interface CandleMapper {
    CandleDTO toDTO(Candle candle);

    @BeforeMapping
    default void checkNull(Candle candle) {
        if (candle == null) {
            throw new IllegalArgumentException("Candle cannot be null.");
        }
    }
}