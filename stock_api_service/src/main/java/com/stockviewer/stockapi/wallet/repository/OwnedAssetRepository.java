package com.stockviewer.stockapi.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.stockviewer.stockapi.wallet.entity.OwnedAsset;
import com.stockviewer.stockapi.wallet.entity.Wallet;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface OwnedAssetRepository extends JpaRepository<OwnedAsset, UUID> {
    Optional<OwnedAsset> findByWalletAndAsset_Symbol(Wallet wallet, String assetSymbol);
    List<OwnedAsset> findAllByWallet(Wallet wallet);
}