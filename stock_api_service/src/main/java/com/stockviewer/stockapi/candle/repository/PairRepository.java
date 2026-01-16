package com.stockviewer.stockapi.candle.repository;

import com.stockviewer.stockapi.candle.entity.Pair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

@Repository
public interface PairRepository extends JpaRepository<Pair, UUID> {
    Pair findBySymbol(String pair);
    Pair findFirstByRiskTolerance(int riskTolerance);
    @Query("SELECT p.baseAsset.symbol FROM Pair p WHERE p.id = :id")
    String findBaseAssetSymbolById(@Param("id") UUID pairId);
}
