package com.stockviewer.stockapi.candle.repository;

import com.stockviewer.stockapi.candle.entity.Candle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.stockviewer.stockapi.candle.entity.Pair;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandleRepository extends JpaRepository<Candle, UUID> {
    List<Candle> findByPair_Symbol(String symbol);
    @EntityGraph(attributePaths = {"pair", "indicators"})
    @Query("""
       SELECT c
       FROM Candle c
       JOIN FETCH c.pair
       LEFT JOIN FETCH c.indicators
       WHERE c.pair.symbol = :symbol
         AND c.timeframe = :timeframe
       ORDER BY c.timestamp ASC
    """)
    List<Candle> findByPair_SymbolAndTimeframe(String symbol, String timeframe);
    Candle getCandleByCandleId(UUID candleId);

    @Query("""
       SELECT c 
       FROM Candle c 
       JOIN FETCH c.pair 
       LEFT JOIN FETCH c.indicators 
       WHERE c.candleId = :id
       """)
    Optional<Candle> findByIdWithPairAndIndicators(@Param("id") UUID id);

    @Query("""
       SELECT c
       FROM Candle c
       WHERE c.pair = :pair
         AND c.timeframe = :timeframe
       ORDER BY c.timestamp DESC
       LIMIT 1
    """)
    Optional<Candle> findTopByPairAndTimeframeOrderByTimestampDesc(@Param("pair") Pair pair, @Param("timeframe") String timeframe);
}