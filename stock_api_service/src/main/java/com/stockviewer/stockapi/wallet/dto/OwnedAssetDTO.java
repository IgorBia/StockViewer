package com.stockviewer.stockapi.wallet.dto;

import java.math.BigDecimal;

public class OwnedAssetDTO {
    private String name;
    private BigDecimal amount;
    private BigDecimal avgPrice;

    public OwnedAssetDTO() {}

    public OwnedAssetDTO(String name, BigDecimal amount, BigDecimal avgPrice) {
        this.name = name;
        this.amount = amount;
        this.avgPrice = avgPrice;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getAvgPrice() { return avgPrice; }
    public void setAvgPrice(BigDecimal avgPrice) { this.avgPrice = avgPrice; }
}