package com.stockviewer.stockapi.candle.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import com.stockviewer.stockapi.indicators.IndicatorDTO;

public record CandleDTO(
        OffsetDateTime timestamp,
        OffsetDateTime closeTime,
        BigDecimal open,
        BigDecimal close,
        BigDecimal high,
        BigDecimal low,
        BigDecimal volume,
        List<IndicatorDTO> indicators
) { }

