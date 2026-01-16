package com.stockviewer.stockapi.candle.service;

import com.stockviewer.stockapi.candle.dto.CandleDTO;
import com.stockviewer.stockapi.candle.entity.Candle;
import com.stockviewer.stockapi.candle.entity.Pair;
import com.stockviewer.stockapi.candle.dto.PairDTO;
import com.stockviewer.stockapi.candle.mapper.CandleMapper;
import com.stockviewer.stockapi.candle.mapper.PairMapper;
import com.stockviewer.stockapi.candle.repository.CandleRepository;
import com.stockviewer.stockapi.candle.repository.PairRepository;
import com.stockviewer.stockapi.exception.ResourceNotFoundException;
import com.stockviewer.stockapi.exception.TimeFrameNotSupported;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.stockviewer.stockapi.candle.service.CandleDataFetcher;

@Service
public class CandleService {

    private final CandleRepository candleRepository;
    private final PairRepository pairRepository;
    private final CandleMapper candleMapper;
    private final PairMapper pairMapper;
    private final CandleConfig candleConfig;

    public CandleService(CandleRepository candleRepository, CandleMapper candleMapper, CandleConfig candleConfig, PairRepository pairRepository, PairMapper pairMapper) {
        this.candleRepository = candleRepository;
        this.candleMapper = candleMapper;
        this.candleConfig = candleConfig;
        this.pairRepository = pairRepository;
        this.pairMapper = pairMapper;
    }

    public List<CandleDTO> getAllCandleDTOs() {
        List<Candle> candles = candleRepository.findAll();
        if(candles.isEmpty()) throw new  ResourceNotFoundException("Candles not found");
        return candles
                .stream()
                .map(candleMapper::toDTO)
                .toList();
    }

    public List<CandleDTO> getCandleDTOsBySymbol(String symbol) {
        List<Candle> candles = candleRepository.findByPair_Symbol(symbol);
        if(candles.isEmpty()) throw new ResourceNotFoundException("Candles not found");
        return candles
                .stream()
                .map(candleMapper::toDTO)
                .toList();
    }

    public List<CandleDTO> getCandlesDTOBySymbolAndTimeframe(String symbol, String timeframe) {
        return getCandlesBySymbolAndTimeframe(symbol, timeframe)
                .stream()
                .map(candleMapper::toDTO)
                .toList();
    }

    public List<Candle> getCandlesBySymbolAndTimeframe(String symbol, String timeframe) {
        validateTimeFrame(timeframe);
        List<Candle> candles = candleRepository.findByPair_SymbolAndTimeframe(symbol, timeframe);
        if(candles.isEmpty()) throw new ResourceNotFoundException("Candles not found");
        return candles;
    }

    public Candle getByIdWithPairAndIndicators(UUID candleId) {
        Optional<Candle> candleOpt = candleRepository.findByIdWithPairAndIndicators(candleId);
        return candleOpt.orElseThrow(() -> new ResourceNotFoundException("Candle not found"));
    }

    private void validateTimeFrame(String timeframe){
        if(!candleConfig.getSupported().contains(timeframe)) {
            throw new TimeFrameNotSupported("Timeframe \"" + timeframe + "\" not supported");
        }
    }

    public CandleDTO getActualCandleData(String symbol, String timeframe) {
        validateTimeFrame(timeframe);
        return new CandleDataFetcher().fetchCandleData(symbol, timeframe);
    }

    public Pair getPair(String symbol) {
        try{
            return pairRepository.findBySymbol(symbol);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to get pair data for symbol: " + symbol);
        }
    }

    public List<Pair> getAllPairs() {
        return pairRepository.findAll();
    }
    
    public BigDecimal getTicker(String symbol) {
        try{ 
            return new CandleDataFetcher().fetchTicker(symbol);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to get ticker price for symbol: " + symbol);
        }
    }

    public PairDTO getPairInfoBySymbol(String symbol) {
        Pair pair = getPair(symbol);
        if (pair == null) {
            throw new ResourceNotFoundException("Pair not found for symbol: " + symbol);
        }
        return pairMapper.toDTO(pair);
    }

    public Pair getPairbyRiskTolerance(int riskTolerance) {
        try{
            return pairRepository.findFirstByRiskTolerance(riskTolerance);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to get pair data for risk tolerance: " + riskTolerance);
        }
    }

    public String findBaseAssetSymbolById(UUID pairId) {
        try{
            return pairRepository.findBaseAssetSymbolById(pairId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to get base asset symbol for pair id: " + pairId);
        }
    }

    public Candle getLatestCandleForPair(Pair pair, String timeframe) {
        validateTimeFrame(timeframe);
        Optional<Candle> candleOpt = candleRepository.findTopByPairAndTimeframeOrderByTimestampDesc(pair, timeframe); 
        return candleOpt.orElseThrow(() -> new ResourceNotFoundException("Candle not found for pair: " + pair.getSymbol() + " and timeframe: " + timeframe));
    }
}