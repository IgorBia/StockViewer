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
import java.math.BigDecimal;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(schema="user_management", name="owned_asset")
public class OwnedAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owned_asset_id")
    private UUID id;

    @JoinColumn(name = "wallet_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Wallet wallet;

    @JoinColumn(name = "asset_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)  
    private Asset asset;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
}
