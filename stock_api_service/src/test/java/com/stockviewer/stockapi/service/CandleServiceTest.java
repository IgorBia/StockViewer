package com.stockviewer.stockapi.service;


import com.stockviewer.stockapi.dto.CandleDTO;
import com.stockviewer.stockapi.entity.Candle;
import com.stockviewer.stockapi.mapper.CandleMapper;
import com.stockviewer.stockapi.repository.CandleRepository;
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

    private final CandleMapper candleMapper = new CandleMapper();

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

    @Test
    void getAllCandlesTest() {

        // given
        List<Candle> candleList = getSampleCandleList();
        when(candleRepository.findAll()).thenReturn(candleList);

        // when
        List<CandleDTO> result = candleService.getAllCandles();

        // then
        assertAll("CandleDTO vs Candle",
                () -> assertEquals(result.size(), candleList.size()),
                () -> assertEquals(candleList.getFirst().getOpenTime(), result.getFirst().getTimestamp()),
                () -> assertEquals(candleList.getFirst().getOpen(), result.getFirst().getOpen()),
                () -> assertEquals(candleList.getFirst().getHigh(), result.getFirst().getHigh()),
                () -> assertEquals(candleList.getFirst().getLow(), result.getFirst().getLow()),
                () -> assertEquals(candleList.getFirst().getClose(), result.getFirst().getClose())
        );
    }

    @Test
    void getCandlesBySymbolTest() {

        // given
        List<Candle> candleList = getSampleCandleList();
        when(candleRepository.findByPair_Symbol(any())).thenReturn(candleList);

        // when
        List<CandleDTO> result = candleService.getCandlesBySymbol("ETHUSDC");

        // then
        assertAll("CandleDTO vs Candle",
                () -> assertEquals(result.size(), candleList.size()),
                () -> assertEquals(candleList.getFirst().getOpenTime(), result.getFirst().getTimestamp()),
                () -> assertEquals(candleList.getFirst().getOpen(), result.getFirst().getOpen()),
                () -> assertEquals(candleList.getFirst().getHigh(), result.getFirst().getHigh()),
                () -> assertEquals(candleList.getFirst().getLow(), result.getFirst().getLow()),
                () -> assertEquals(candleList.getFirst().getClose(), result.getFirst().getClose())
        );
    }

}
