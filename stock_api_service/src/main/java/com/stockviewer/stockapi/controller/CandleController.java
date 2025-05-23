package com.stockviewer.stockapi.controller;

import ch.qos.logback.classic.Logger;
import com.stockviewer.stockapi.dto.CandleDTO;
import com.stockviewer.stockapi.service.CandleService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candles")
public class CandleController {

    private final CandleService candleService;

    Logger logger = (Logger) LoggerFactory.getLogger(CandleController.class);

    public CandleController(CandleService candleService) {
        this.candleService = candleService;
    }


    @GetMapping("/all")
    public List<CandleDTO> getAllCandles() {
        // TODO: logging about receiving a request from IP
        return candleService.getAllCandles();
    }

    @GetMapping("/{symbol}")
    public List<CandleDTO> getCandlesBySymbol(@PathVariable String symbol) {
        // TODO: logging about receiving a request for a symbol from IP
        return candleService.getCandlesBySymbol(symbol);
    }
}