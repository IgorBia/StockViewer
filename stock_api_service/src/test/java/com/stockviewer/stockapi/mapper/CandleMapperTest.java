package com.stockviewer.stockapi.mapper;

import com.stockviewer.stockapi.dto.CandleDTO;
import com.stockviewer.stockapi.entity.Candle;
import org.mapstruct.factory.Mappers;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

        // when
        CandleDTO candleDTO = candleMapper.toDTO(candle);

        // then
        assertAll("CandleDTO vs Candle",
                () -> assertEquals(candle.getTimestamp(), candleDTO.getTimestamp()),
                () -> assertEquals(candle.getOpen(), candleDTO.getOpen()),
                () -> assertEquals(candle.getClose(), candleDTO.getClose()),
                () -> assertEquals(candle.getHigh(), candleDTO.getHigh()),
                () -> assertEquals(candle.getLow(), candleDTO.getLow())
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
