package com.stockviewer.stockapi.candle.service;


import com.stockviewer.stockapi.candle.dto.CandleDTO;
import com.stockviewer.stockapi.candle.entity.Candle;
import com.stockviewer.stockapi.candle.mapper.CandleMapper;
import org.mapstruct.factory.Mappers;
import com.stockviewer.stockapi.candle.repository.CandleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CandleServiceTest {

    @Mock
    private CandleRepository candleRepository;

    CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);

    private CandleService candleService;

    @BeforeEach
    void setUp() {
        candleService = new CandleService(candleRepository, candleMapper);
    }

    private List<Candle> getSampleCandleList() {
        Candle candle = new Candle(
                LocalDateTime.of(2024, 11, 7, 15, 30),
                new BigDecimal("1234.56"),
                new BigDecimal("1250.78"),
                new BigDecimal("1260.90"),
                new BigDecimal("1220.33")
        );
        return List.of(candle);
    }

    private void assertCandleListValid(List<Candle> expected, List<CandleDTO> actual) {
        assertAll("CandleDTO vs Candle",
                () -> assertEquals(expected.size(), actual.size()),
                () -> {
                    Candle firstExpected = expected.getFirst();
                    CandleDTO firstActual = actual.getFirst();
                    assertEquals(firstExpected.getTimestamp(), firstActual.getTimestamp());
                    assertEquals(firstExpected.getOpen(), firstActual.getOpen());
                    assertEquals(firstExpected.getHigh(), firstActual.getHigh());
                    assertEquals(firstExpected.getLow(), firstActual.getLow());
                    assertEquals(firstExpected.getClose(), firstActual.getClose());
                }
        );
    }

    @Test
    void getAllCandlesTest() {

        // given
        List<Candle> candleList = getSampleCandleList();
        when(candleRepository.findAll()).thenReturn(candleList);

        // when
        List<CandleDTO> result = candleService.getAllCandles();

        // then
        assertCandleListValid(candleList, result);
    }

    @Test
    void getCandlesBySymbolTest() {

        // given
        List<Candle> candleList = getSampleCandleList();
        when(candleRepository.findByPair_Symbol(any())).thenReturn(candleList);

        // when
        List<CandleDTO> result = candleService.getCandlesBySymbol("ETHUSDC");

        // then
        assertCandleListValid(candleList, result);
    }

}
