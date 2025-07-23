package com.stockviewer.stockapi.candle.repository;

import com.stockviewer.stockapi.candle.entity.Candle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandleRepository extends JpaRepository<Candle, Long> {
    List<Candle> findByPair_Symbol(String symbol);
}