package com.stockviewer.stockapi.candle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import com.stockviewer.stockapi.wallet.entity.Asset;

import java.util.UUID;

@Getter
@Entity
@Table(schema="stock_data", name="pair")
public class Pair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="pair_id")
    private UUID id;

    private String symbol;

    @ManyToOne
    @JoinColumn(name="base_asset_id", nullable=false)
    private Asset baseAsset;

    @ManyToOne
    @JoinColumn(name="quote_asset_id", nullable=false)
    private Asset quoteAsset;

    private String market;
    private String exchange;

    @Column(name="risk_tolerance", nullable=false)
    private int riskTolerance;
}