package com.stockviewer.stockapi.candle.mapper;

import com.stockviewer.stockapi.candle.dto.CandleDTO;
import com.stockviewer.stockapi.candle.entity.Candle;
import org.mapstruct.factory.Mappers;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class CandleMapperTest {

    CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);

    @Test
    void validCandleToDTOTest() {

        // given
        Candle candle = new Candle(
                LocalDateTime.of(2024, 11, 7, 15, 30),
                new BigDecimal("1234.56"),
                new BigDecimal("1250.78"),
                new BigDecimal("1260.90"),
                new BigDecimal("1220.33")
        );
        candle.setIndicators(List.of()); // pusty dla testu
        candle.setVolume(BigDecimal.ZERO);
        candle.setCloseTime(LocalDateTime.of(2024, 11, 7, 15, 31));

        // when
        CandleDTO candleDTO = candleMapper.toDTO(candle);

        // then
        assertAll("CandleDTO vs Candle",
            () -> assertEquals(candle.getTimestamp(), candleDTO.timestamp().toLocalDateTime()),
            () -> assertEquals(candle.getOpen(), candleDTO.open()),
            () -> assertEquals(candle.getClose(), candleDTO.close()),
            () -> assertEquals(candle.getHigh(), candleDTO.high()),
            () -> assertEquals(candle.getLow(), candleDTO.low())
        );
    }

    @Test
    void emptyCandleToDTOTest() {

        // given
        Candle candle = null;

        // when + then
        assertThrows(IllegalArgumentException.class, () -> candleMapper.toDTO(candle));
    }
}
