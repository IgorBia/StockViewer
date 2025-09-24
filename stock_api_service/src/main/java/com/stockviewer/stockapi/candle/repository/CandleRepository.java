package com.stockviewer.stockapi.candle.repository;

import com.stockviewer.stockapi.candle.entity.Candle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandleRepository extends JpaRepository<Candle, UUID> {
    List<Candle> findByPair_Symbol(String symbol);
    List<Candle> findByPair_SymbolAndTimeframe(String symbol, String timeframe);

}