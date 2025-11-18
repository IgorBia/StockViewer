package com.stockviewer.stockapi.wallet.service;

import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.wallet.entity.OwnedAsset;
import com.stockviewer.stockapi.wallet.entity.Wallet;
import com.stockviewer.stockapi.wallet.repository.AssetRepository;
import com.stockviewer.stockapi.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final AssetRepository assetRepository;

    public WalletService(WalletRepository walletRepository, AssetRepository assetRepository) {
        this.walletRepository = walletRepository;
        this.assetRepository = assetRepository;
    }

    public Wallet createWalletForUser(User user) {
        Wallet wallet = new Wallet(user);


        assetRepository.findBySymbol("USDC").ifPresent(asset -> {
            OwnedAsset ownedAsset = new OwnedAsset();
            ownedAsset.setAsset(asset);
            ownedAsset.setAmount(new BigDecimal("10000"));
            wallet.addOwnedAsset(ownedAsset);
        });

        return walletRepository.save(wallet);
    }
}