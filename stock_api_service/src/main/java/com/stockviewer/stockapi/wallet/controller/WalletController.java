package com.stockviewer.stockapi.wallet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Set;
import com.stockviewer.stockapi.wallet.dto.OwnedAssetDTO;

import org.springframework.http.ResponseEntity;
import com.stockviewer.stockapi.wallet.dto.WalletResponse;

import com.stockviewer.stockapi.wallet.service.WalletService;

@RestController
@RequestMapping("wallet")
public class WalletController {
    
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<Set<OwnedAssetDTO>> getWallet() {
        return ResponseEntity.ok(walletService.getOwnedAssetsByUser());
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<OwnedAssetDTO> getOwnedAsset(@PathVariable String symbol){
        return ResponseEntity.ok(walletService.getOwnedAssetDTOBySymbol(symbol));
    }

    @GetMapping("snapshots")
    public ResponseEntity<?> getWalletSnapshots() {
        return ResponseEntity.ok(walletService.getWalletWorthSnapshots());
    }
}
