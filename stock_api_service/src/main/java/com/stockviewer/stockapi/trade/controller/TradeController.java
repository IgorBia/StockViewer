package com.stockviewer.stockapi.trade.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stockviewer.stockapi.trade.dto.TradeDTO;
import com.stockviewer.stockapi.trade.dto.TradeRequest;
import com.stockviewer.stockapi.trade.dto.TransactionType;
import com.stockviewer.stockapi.trade.service.TradeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("trade")
public class TradeController {
    
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/execute")
    public void executeTrade(@Valid @RequestBody TradeRequest tradeRequest) {
        tradeService.executeTrade(
            tradeRequest.pairSymbol(), 
            tradeRequest.amount(), 
            TransactionType.valueOf(tradeRequest.transactionType()));
    }

    @GetMapping("/history")
    public List<TradeDTO> getTradeHistoryForUser() {
        return tradeService.getTradeHistoryForUser();
    }
}
