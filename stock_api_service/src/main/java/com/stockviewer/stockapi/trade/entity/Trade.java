package com.stockviewer.stockapi.trade.entity;

import jakarta.persistence.*;
import java.util.UUID;
import com.stockviewer.stockapi.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stockviewer.stockapi.candle.entity.Pair;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema="user_management", name = "trade")
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private UUID tradeId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pair_id", nullable = false)
    private Pair pair;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "timestamp", nullable = false)
    private java.time.LocalDateTime timestamp;

    @Column(name = "price", nullable = false, precision = 18, scale = 8)
    private java.math.BigDecimal price;

    @Column(name = "base_amount", nullable = false, precision = 18, scale = 8)
    private java.math.BigDecimal baseAmount;

    @Column(name = "quote_amount", nullable = false, precision = 18, scale = 8)
    private java.math.BigDecimal quoteAmount;

    @Column(name = "pnl", precision = 18, scale = 8)
    private java.math.BigDecimal pnl;

    @Column(name = "stop_loss", precision = 18, scale = 8)
    private java.math.BigDecimal stopLoss;

    @Column(name = "take_profit", precision = 18, scale = 8)
    private java.math.BigDecimal takeProfit;
}
