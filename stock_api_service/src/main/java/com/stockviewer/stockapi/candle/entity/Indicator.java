package com.stockviewer.stockapi.candle.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "indicator", schema = "stock_data")
public class Indicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candle_id", nullable = false)
    private Candle candle;

    @Column(nullable = false)
    private String name;

    private BigDecimal value;

    @Column(nullable = false)
    private Instant ts;
}