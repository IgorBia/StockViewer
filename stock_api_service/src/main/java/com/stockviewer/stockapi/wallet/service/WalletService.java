package com.stockviewer.stockapi.wallet.service;

import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.wallet.entity.OwnedAsset;
import com.stockviewer.stockapi.wallet.entity.Wallet;
import com.stockviewer.stockapi.wallet.entity.WalletWorthSnapshot;
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
import com.stockviewer.stockapi.wallet.mapper.OwnedAssetMapper;
import com.stockviewer.stockapi.wallet.entity.OwnedAsset;
import com.stockviewer.stockapi.wallet.repository.OwnedAssetRepository;
import java.util.List;
import jakarta.transaction.Transactional;


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
    private final OwnedAssetMapper ownedAssetMapper;
    private final OwnedAssetRepository ownedAssetRepository;
    

    public WalletService(WalletRepository walletRepository, AssetRepository assetRepository, UserService userService, CandleService candleService, OwnedAssetMapper ownedAssetMapper, OwnedAssetRepository ownedAssetRepository) {
        this.walletRepository = walletRepository;
        this.assetRepository = assetRepository;
        this.userService = userService;
        this.candleService = candleService;
        this.ownedAssetMapper = ownedAssetMapper;
        this.ownedAssetRepository = ownedAssetRepository;
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
                .map(ownedAssetMapper::toDTO)
                .filter(ownedDTO -> ownedDTO.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toSet());

        return ownedAssets;
    }

    public Set<OwnedAssetDTO> getOwnedAssetsByUser(User user) {
        Wallet wallet = walletRepository.findByUser(user)
                .orElseGet(() -> createWalletForUser(user));

        // pobieramy OwnedAssety przez repository aby nie inicjalizować lazy kolekcji wallet.getOwnedAssets()
        List<OwnedAsset> ownedList = ownedAssetRepository.findAllByWallet(wallet);

        Set<OwnedAssetDTO> ownedAssets = ownedList.stream()
                .map(ownedAssetMapper::toDTO)
                .filter(ownedDTO -> ownedDTO.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toSet());

        return ownedAssets;
    }

    public BigDecimal getOwnedAssetAmount(String assetSymbol) {
        User user = userService.getUserFromContext();
        Wallet wallet = walletRepository.findByUser(user)
                .orElseGet(() -> createWalletForUser(user));

        return wallet.getOwnedAssets().stream()
                .filter(owned -> owned.getAsset().getSymbol().equals(assetSymbol))
                .findFirst()
                .map(OwnedAsset::getAmount)
                .orElse(BigDecimal.ZERO);
    }

    public OwnedAssetDTO getOwnedAssetDTOBySymbol(String assetSymbol) {
        User user = userService.getUserFromContext();
        Wallet wallet = walletRepository.findByUser(user)
                .orElseGet(() -> createWalletForUser(user));

        return wallet.getOwnedAssets().stream()
                .filter(owned -> owned.getAsset().getSymbol().equals(assetSymbol))
                .findFirst()
                .map(ownedAssetMapper::toDTO)
                .orElse(new OwnedAssetDTO(assetSymbol, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    public Wallet getWallet(User user) {
        return walletRepository.findByUser(user)
                .orElseGet(() -> createWalletForUser(user));
    }

    @Transactional
    public void updateOwnedAsset(Wallet wallet, String assetSymbol, BigDecimal amountChange, BigDecimal price) {
        OwnedAsset ownedAsset = getOwnedAsset(wallet, assetSymbol);

        BigDecimal currentAmount = ownedAsset.getAmount();
        if (currentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            ownedAsset.setAvgPrice(price);
            updateOwnedAsset(wallet, assetSymbol, amountChange);
            return;
        } 

        BigDecimal currentAvgPrice = ownedAsset.getAvgPrice();
        BigDecimal newAmount = currentAmount.add(amountChange);

        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            ownedAsset.setAvgPrice(BigDecimal.ZERO);
            updateOwnedAsset(wallet, assetSymbol, amountChange);
            return;
        }
        BigDecimal newAvgPrice = (currentAvgPrice.multiply(currentAmount).add(price.multiply(amountChange)))
                .divide(newAmount, 8, RoundingMode.HALF_UP);
        ownedAsset.setAvgPrice(newAvgPrice);
        updateOwnedAsset(wallet, assetSymbol, amountChange);
    }

    @Transactional
    public void updateOwnedAsset(Wallet wallet, String assetSymbol, BigDecimal amountChange) {
        OwnedAsset ownedAsset = getOwnedAsset(wallet, assetSymbol);

        BigDecimal newAmount = ownedAsset.getAmount().add(amountChange);
        ownedAsset.setAmount(newAmount.setScale(8, RoundingMode.HALF_UP));
        // persist via repository to avoid relying on lazy collection initialization
        ownedAssetRepository.save(ownedAsset);
        walletRepository.save(wallet);
    }

    // load via repository to avoid initializing lazy collection outside session
    public OwnedAsset getOwnedAsset(Wallet wallet, String assetSymbol) {
        return ownedAssetRepository.findByWalletAndAsset_Symbol(wallet, assetSymbol)
            .orElseGet(() -> {
                OwnedAsset newOwnedAsset = new OwnedAsset();
                assetRepository.findBySymbol(assetSymbol).ifPresent(newOwnedAsset::setAsset);
                newOwnedAsset.setAmount(BigDecimal.ZERO);
                // ensure wallet relation set before saving to avoid null wallet_id
                newOwnedAsset.setWallet(wallet);
                OwnedAsset saved = ownedAssetRepository.save(newOwnedAsset);
                // don't touch wallet collection here — wallet may be detached
                return saved;
            });
    }

    public List<WalletWorthSnapshot> getWalletWorthSnapshots() {
        Wallet wallet = getWallet(userService.getUserFromContext());
        return wallet.getWorthSnapshots();
    }

    public BigDecimal getOwnedAssetAmountForWallet(Wallet wallet, String assetSymbol) {
        return ownedAssetRepository.findByWalletAndAsset_Symbol(wallet, assetSymbol)
                .map(OwnedAsset::getAmount)
                .orElse(BigDecimal.ZERO);
    }
    public OwnedAsset getOwnedAssetEntityForWallet(Wallet wallet, String assetSymbol) {
    return ownedAssetRepository.findByWalletAndAsset_Symbol(wallet, assetSymbol)
            .orElse(null);
    }
}