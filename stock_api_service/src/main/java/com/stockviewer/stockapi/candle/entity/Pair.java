package com.stockviewer.stockapi.candle.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
@Table(schema="stock_data", name="pair")
public class Pair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="pair_id")
    private UUID pairId;
    private String symbol;
}
