package com.stockviewer.stockapi.controller;

import com.stockviewer.stockapi.dto.CandleDTO;
import com.stockviewer.stockapi.service.CandleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candles")
public class CandleController {

    private final CandleService candleService;

    public CandleController(CandleService candleService) {
        this.candleService = candleService;
    }

    @GetMapping
    public List<CandleDTO> getAllCandles() {
        return candleService.getAllCandles();
    }

    @GetMapping("/{symbol}")
    public List<CandleDTO> getCandlesBySymbol(@PathVariable String symbol) {
        return candleService.getCandlesBySymbol(symbol);
    }
}