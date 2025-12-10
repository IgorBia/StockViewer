package com.stockviewer.stockapi.wallet.service;

import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.wallet.entity.OwnedAsset;
import com.stockviewer.stockapi.wallet.entity.Wallet;
import com.stockviewer.stockapi.user.service.UserService;
import com.stockviewer.stockapi.candle.entity.Pair;
import com.stockviewer.stockapi.candle.service.CandleService;
import com.stockviewer.stockapi.wallet.repository.AssetRepository;
import com.stockviewer.stockapi.wallet.repository.WalletRepository;
import com.stockviewer.stockapi.candle.repository.PairRepository;
import com.stockviewer.stockapi.wallet.dto.AmountsDTO;
import com.stockviewer.stockapi.wallet.dto.OwnedAssetDTO;
import com.stockviewer.stockapi.wallet.dto.WalletResponse;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final AssetRepository assetRepository;
    private final UserService userService;
    private final CandleService candleService;

    public WalletService(WalletRepository walletRepository, AssetRepository assetRepository, UserService userService, CandleService candleService) {
        this.walletRepository = walletRepository;
        this.assetRepository = assetRepository;
        this.userService = userService;
        this.candleService = candleService;
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

    public Set<OwnedAssetDTO> getOwnedAssetsByUser() {
        User user = userService.getUserFromContext();
        Wallet wallet = walletRepository.findByUser(user)
                .orElseGet(() -> createWalletForUser(user));

        Set<OwnedAssetDTO> ownedAssets = wallet.getOwnedAssets().stream()
                .map(owned -> new OwnedAssetDTO(
                        owned.getAsset().getSymbol(),
                        owned.getAmount()
                )).collect(Collectors.toSet());

        return ownedAssets;
    }

    public Wallet getWallet(User user) {
        return walletRepository.findByUser(user)
                .orElseGet(() -> createWalletForUser(user));
    }

    public void updateOwnedAsset(Wallet wallet, String assetSymbol, BigDecimal amountChange) {
        OwnedAsset ownedAsset = wallet.getOwnedAssets().stream()
                .filter(owned -> owned.getAsset().getSymbol().equals(assetSymbol))
                .findFirst()
                .orElseGet(() -> {
                    OwnedAsset newOwnedAsset = new OwnedAsset();
                    assetRepository.findBySymbol(assetSymbol).ifPresent(newOwnedAsset::setAsset);
                    newOwnedAsset.setAmount(BigDecimal.ZERO);
                    wallet.addOwnedAsset(newOwnedAsset);
                    return newOwnedAsset;
                });

        BigDecimal newAmount = ownedAsset.getAmount().add(amountChange);
        ownedAsset.setAmount(newAmount.setScale(8, RoundingMode.HALF_UP));
        walletRepository.save(wallet);
    }
}