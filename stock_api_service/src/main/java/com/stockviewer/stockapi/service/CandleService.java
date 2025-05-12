package com.stockviewer.stockapi.service;

import com.stockviewer.stockapi.dto.CandleDTO;
import com.stockviewer.stockapi.entity.Candle;
import com.stockviewer.stockapi.repository.CandleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandleService {

    private final CandleRepository candleRepository;

    public CandleService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    public List<CandleDTO> getAllCandles() {
        return candleRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public List<CandleDTO> getCandlesBySymbol(String symbol) {
        List<Candle> candles = candleRepository.findByPair_Symbol(symbol);
        return candles.stream().map(this::mapToDTO).toList();
    }

    private CandleDTO mapToDTO(Candle candle) {
        CandleDTO dto = new CandleDTO();
        dto.setOpen(candle.getOpen());
        dto.setClose(candle.getClose());
        dto.setTimestamp(candle.getOpenTime());
        dto.setHigh(candle.getHigh());
        dto.setLow(candle.getLow());
        return dto;
    }
}