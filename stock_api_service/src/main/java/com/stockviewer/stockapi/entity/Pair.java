package com.stockviewer.stockapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Pair {
    @Id
    private Long pair_id;
    private String symbol;

    public Long getPairId() {
        return pair_id;
    }
    public String getSymbol() {
        return symbol;
    }

}
