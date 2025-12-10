package com.stockviewer.stockapi.trade.dto;

import java.math.BigDecimal;

public record TradeRequest(String pairSymbol, BigDecimal amount, String transactionType) {
}
