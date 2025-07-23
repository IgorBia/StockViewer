package com.stockviewer.stockapi.candle.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandleDTO {
    private LocalDateTime timestamp;
    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal high;
    private BigDecimal low;

    public CandleDTO(LocalDateTime timestamp, BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low) {
        this.timestamp = timestamp;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
    }
}

