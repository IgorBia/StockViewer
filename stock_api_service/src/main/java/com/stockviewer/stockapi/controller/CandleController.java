package com.stockviewer.stockapi.controller;

import ch.qos.logback.classic.Logger;
import com.stockviewer.stockapi.dto.CandleDTO;
import com.stockviewer.stockapi.service.CandleService;
import com.stockviewer.stockapi.utility.HttpHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candles")
public class CandleController {

    private final CandleService candleService;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(CandleController.class);

    public CandleController(CandleService candleService) {
        this.candleService = candleService;
    }


    @GetMapping("/all")
    public List<CandleDTO> getAllCandles(HttpServletRequest request) {
        logger.info("Received request from IP: {}", HttpHelper.getClientIP(request));
        return candleService.getAllCandles();
    }

    @GetMapping("/{symbol}")
    public List<CandleDTO> getCandlesBySymbol(@PathVariable String symbol, HttpServletRequest request) {
        logger.info("Received request for symbol {} from IP: {}", symbol, HttpHelper.getClientIP(request));
        return candleService.getCandlesBySymbol(symbol);
    }
}