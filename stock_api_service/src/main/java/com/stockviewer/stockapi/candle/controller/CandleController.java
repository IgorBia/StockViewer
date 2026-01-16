package com.stockviewer.stockapi.candle.controller;

import com.stockviewer.stockapi.candle.dto.CandleDTO;
import com.stockviewer.stockapi.candle.dto.CandleResponse;
import com.stockviewer.stockapi.candle.service.CandleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.stockviewer.stockapi.candle.dto.PairDTO;

@RestController
@RequestMapping("candles")
public class CandleController {

    private final CandleService candleService;

    public CandleController(CandleService candleService) {
        this.candleService = candleService;
    }


    @GetMapping("/all")
    public ResponseEntity<CandleResponse> getAllCandles() {
        return ResponseEntity
                .status(200)
                .body(new CandleResponse(candleService.getAllCandleDTOs()));
    }

    @GetMapping("/{symbol}/{timeframe}")
    public ResponseEntity<CandleResponse> getCandlesBySymbolAndTimeframe(@PathVariable String symbol, @PathVariable String timeframe) {
        return ResponseEntity
                .status(200)
                .body(new CandleResponse(candleService.getCandlesDTOBySymbolAndTimeframe(symbol, timeframe)));
    }

    @GetMapping("/{symbol}/{timeframe}/refresh")
    public ResponseEntity<CandleDTO> refreshCandleData(@PathVariable String symbol, @PathVariable String timeframe) {
        return ResponseEntity
                .status(200)
                .body(candleService.getActualCandleData(symbol, timeframe));
    }

    @GetMapping("/{symbol}/info")
    public ResponseEntity<PairDTO> getPairInfoBySymbol(@PathVariable String symbol) {
        return ResponseEntity
                .status(200)
                .body(candleService.getPairInfoBySymbol(symbol));
    }
}