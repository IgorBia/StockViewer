package com.stockviewer.stockapi.wallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stockviewer.stockapi.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(schema="stock_data", name="asset")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private UUID id;

    @Column(name = "symbol", nullable = false, unique = true)
    private String symbol;

    @Column(name = "display_name", nullable = false)
    private String displayName;
}
