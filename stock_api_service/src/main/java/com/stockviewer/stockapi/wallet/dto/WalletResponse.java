package com.stockviewer.stockapi.wallet.dto;

import java.util.Set;

public class WalletResponse {
    private Set<OwnedAssetDTO> assets;

    public WalletResponse() {}

    public WalletResponse(Set<OwnedAssetDTO> assets) {
        this.assets = assets;
    }

    public Set<OwnedAssetDTO> getAssets() { return assets; }
    public void setAssets(Set<OwnedAssetDTO> assets) { this.assets = assets; }
}