package com.stockviewer.stockapi.candle.dto;

import com.stockviewer.stockapi.wallet.entity.Asset;
import java.util.UUID;

public record PairDTO(
        UUID id,
        String symbol,
        Asset baseAsset,
        Asset quoteAsset,
        String market,
        String exchange
) { }