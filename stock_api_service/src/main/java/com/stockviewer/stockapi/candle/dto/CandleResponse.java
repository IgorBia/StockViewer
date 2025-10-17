package com.stockviewer.stockapi.candle.dto;

import java.util.List;

public record CandleResponse(List<CandleDTO> candles) {
}
