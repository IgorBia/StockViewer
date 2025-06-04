package com.stockviewer.stockapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Getter;

@Getter
@Entity
public class Pair {

    @Id
    @Column(name="pair_id")
    private Long pairId;
    private String symbol;
}
