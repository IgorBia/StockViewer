package com.stockviewer.stockapi.candle.service;

import com.stockviewer.stockapi.candle.dto.CandleDTO;
import com.stockviewer.stockapi.candle.mapper.CandleMapper;
import com.stockviewer.stockapi.candle.repository.CandleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandleService {

    private final CandleRepository candleRepository;
    private final CandleMapper candleMapper;

    public CandleService(CandleRepository candleRepository, CandleMapper candleMapper) {
        this.candleRepository = candleRepository;
        this.candleMapper = candleMapper;
    }

    public List<CandleDTO> getAllCandles() {
        return candleRepository.findAll()
                .stream()
                .map(candleMapper::toDTO)
                .toList();
    }

    public List<CandleDTO> getCandlesBySymbol(String symbol) {
        return candleRepository.findByPair_Symbol(symbol)
                .stream()
                .map(candleMapper::toDTO)
                .toList();
    }
}