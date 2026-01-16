package com.stockviewer.stockapi.wallet.service;
import com.stockviewer.stockapi.wallet.entity.Wallet;
import com.stockviewer.stockapi.wallet.entity.Asset;
import com.stockviewer.stockapi.wallet.entity.WalletWorthSnapshot;
import com.stockviewer.stockapi.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.stockviewer.stockapi.candle.service.CandleService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime; 

@Service
public class WalletWorthSnapshotService {
    private final WalletRepository walletRepository;
    private final CandleService candleService;

    @Autowired
    public WalletWorthSnapshotService(WalletRepository walletRepository, CandleService candleService) {
        this.walletRepository = walletRepository;
        this.candleService = candleService;
    }

    public void takeSnapshot(Wallet wallet) {
        BigDecimal totalWorthUsd = calculateWalletWorth(wallet);
        WalletWorthSnapshot snapshot = new WalletWorthSnapshot();
        snapshot.setWallet(wallet);
        snapshot.setTimestamp(LocalDateTime.now());
        snapshot.setTotalWorthUsd(totalWorthUsd);
        wallet.getWorthSnapshots().add(snapshot);
        walletRepository.save(wallet);
    }

    private BigDecimal calculateWalletWorth(Wallet wallet) {
        return wallet.getOwnedAssets().stream()
                .map(ownedAsset -> {
                    BigDecimal assetPriceUsd = getAssetPriceInUsd(ownedAsset.getAsset());
                    return assetPriceUsd.multiply(ownedAsset.getAmount());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getAssetPriceInUsd(Asset asset) {
        if(asset.getSymbol().equalsIgnoreCase("USDC")) {
            return BigDecimal.ONE;
        }
        return candleService.getTicker(asset.getSymbol()+"USDC");
    }
}