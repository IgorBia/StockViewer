package com.stockviewer.stockapi.candle.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "indicator", schema = "stock_data")
public class Indicator {

    public Indicator(String name, Candle candle, BigDecimal value, LocalDateTime ts){
        this.setIndicatorId(new IndicatorId(candle.getCandleId(), name));
        this.setCandle(candle);
        this.setValue(value);
        this.setTs(ts);
    }

    @EmbeddedId
    private IndicatorId indicatorId;

    @JsonIgnore
    @MapsId("candleId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candle_id", nullable = false)
    private Candle candle;

    private BigDecimal value;

    @Column(nullable = false)
    private LocalDateTime ts;
}