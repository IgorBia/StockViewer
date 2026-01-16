package com.stockviewer.stockapi.candle.service;

import com.stockviewer.stockapi.candle.dto.CandleDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.Instant;

public class CandleParser{

    public CandleParser() {
    }

    public CandleDTO parseDTO(String responseBody) {
        ObjectMapper mapper = new ObjectMapper();
        List<List<Object>> klines;
        try{
            klines = mapper.readValue(responseBody, new TypeReference<>() {});
        } catch(Exception e){
            throw new RuntimeException("Failed to parse candle data", e);
        }
        
        List<CandleDTO> candles = klines.stream()
        .map(r -> new CandleDTO(
            Instant.ofEpochMilli(((Number) r.get(0)).longValue()).atOffset(ZoneOffset.UTC), //OffsetDateTime
            Instant.ofEpochMilli(((Number) r.get(6)).longValue()).atOffset(ZoneOffset.UTC), //OffsetDateTime
            new BigDecimal((String) r.get(1)),
            new BigDecimal((String) r.get(4)),
            new BigDecimal((String) r.get(2)),
            new BigDecimal((String) r.get(3)),
            new BigDecimal((String) r.get(7)),
            null // Indicators can be set later
        ))
        .toList();

        return candles.get(0); // Since we requested limit=1, return the first candle
    }

    public BigDecimal parseTicker(String responseBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            var node = mapper.readTree(responseBody);
            String priceStr = node.get("price").asText();
            return new BigDecimal(priceStr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ticker data", e);
        }
    }
}
