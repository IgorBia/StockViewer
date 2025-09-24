package com.stockviewer.stockapi.candle.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema="stock_data", name = "candle")
public class Candle {

    public Candle(LocalDateTime openTime, BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low) {
        this.timestamp = openTime;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "candle_id")
    private UUID candleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pair_id")
    private Pair pair;

    @Column(name = "open_time", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "open", nullable = false, precision = 18, scale = 8)
    private BigDecimal open;

    @Column(name = "high", nullable = false, precision = 18, scale = 8)
    private BigDecimal high;

    @Column(name = "low", nullable = false, precision = 18, scale = 8)
    private BigDecimal low;

    @Column(name = "close", nullable = false, precision = 18, scale = 8)
    private BigDecimal close;

    @Column(name = "volume", nullable = false, precision = 18, scale = 8)
    private BigDecimal volume;

    @Column(name = "close_time", nullable = false)
    private LocalDateTime closeTime;

    @Column(name = "quote_volume", nullable = false, precision = 18, scale = 8)
    private BigDecimal quoteVolume;

    @Column(name = "trades", nullable = false)
    private Integer trades;

    @Column(name = "taker_base_vol", nullable = false, precision = 18, scale = 8)
    private BigDecimal takerBaseVol;

    @Column(name = "taker_quote_vol", nullable = false, precision = 18, scale = 8)
    private BigDecimal takerQuoteVol;

    @Column(name = "timeframe", nullable = false, length = 10)
    private String timeframe;

    @OneToMany(mappedBy = "candle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Indicator> indicators;

    public UUID getPairId() {
        return getPair().getPairId();
    }
}