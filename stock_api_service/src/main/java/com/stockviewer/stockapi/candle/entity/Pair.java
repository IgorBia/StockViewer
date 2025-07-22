package com.stockviewer.stockapi.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(schema="stock_data", name="pair")
public class Pair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="pair_id")
    private Long pairId;
    private String symbol;
}
