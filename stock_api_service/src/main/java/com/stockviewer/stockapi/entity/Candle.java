package com.stockviewer.stockapi.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "candle")
public class Candle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "candle_id")
    private Integer candleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pair_id")
    private Pair pair;

    @Column(name = "open_time", nullable = false)
    private LocalDateTime openTime;

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

    // Getters and Setters

    public Integer getCandleId() {
        return candleId;
    }

    public void setCandleId(Integer candleId) {
        this.candleId = candleId;
    }

    public Pair getPair() {
        return pair;
    }

    public Long getPairId() {
        return getPair().getPairId();
    }

    public LocalDateTime getOpenTime() {
        return openTime;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public LocalDateTime getCloseTime() {
        return closeTime;
    }

    public BigDecimal getQuoteVolume() {
        return quoteVolume;
    }

    public Integer getTrades() {
        return trades;
    }

    public BigDecimal getTakerBaseVol() {
        return takerBaseVol;
    }

    public BigDecimal getTakerQuoteVol() {
        return takerQuoteVol;
    }

    public String getTimeframe() {
        return timeframe;
    }
}