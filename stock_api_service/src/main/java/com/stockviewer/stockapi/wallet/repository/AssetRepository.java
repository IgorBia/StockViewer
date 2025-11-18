package com.stockviewer.stockapi.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.stockviewer.stockapi.wallet.entity.Asset;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    Optional<Asset> findBySymbol(String symbol);
}