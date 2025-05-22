package com.stockviewer.stockapi.service;

import com.stockviewer.stockapi.dto.CandleDTO;
import com.stockviewer.stockapi.mapper.CandleMapper;
import com.stockviewer.stockapi.repository.CandleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandleService {

    private final CandleRepository candleRepository;
    private final CandleMapper candleMapper = new CandleMapper();

    public CandleService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
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