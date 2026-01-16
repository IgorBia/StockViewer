package com.stockviewer.stockapi.wallet.entity;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(schema="user_management", name="wallet_worth_snapshot")
public class WalletWorthSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private UUID snapshotId;

    @JsonIgnore
    @JoinColumn(name = "wallet_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Wallet wallet;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "total_worth_usd", nullable = false)
    private BigDecimal totalWorthUsd;
}