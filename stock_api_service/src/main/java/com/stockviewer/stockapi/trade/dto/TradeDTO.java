package com.stockviewer.stockapi.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeDTO(String pairSymbol, String transactionType, LocalDateTime timestamp, BigDecimal baseAmount, BigDecimal quoteAmount, BigDecimal price) {
    
}
