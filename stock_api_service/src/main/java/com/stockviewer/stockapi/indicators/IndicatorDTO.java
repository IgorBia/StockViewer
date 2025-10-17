package com.stockviewer.stockapi.indicators;

import java.math.BigDecimal;

public record IndicatorDTO(String name, BigDecimal value) {
}
