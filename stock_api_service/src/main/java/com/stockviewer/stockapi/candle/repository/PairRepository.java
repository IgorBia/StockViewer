package com.stockviewer.stockapi.candle.repository;

import com.stockviewer.stockapi.candle.entity.Pair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PairRepository extends JpaRepository<Pair, UUID> {
    Pair findBySymbol(String pair);
}
