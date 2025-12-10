package com.stockviewer.stockapi.indicators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockviewer.stockapi.candle.entity.Candle;
import com.stockviewer.stockapi.candle.entity.Indicator;
import com.stockviewer.stockapi.candle.service.CandleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IndicatorListener {

    private final static Logger logger = LoggerFactory.getLogger(IndicatorListener.class);
    private final IndicatorCalculator indicatorCalculator;
    private final CandleService candleService;
    private final IndicatorRepository indicatorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public IndicatorListener(IndicatorCalculator indicatorCalculator, CandleService candleService, IndicatorRepository indicatorRepository) {
        this.indicatorCalculator = indicatorCalculator;
        this.candleService = candleService;
        this.indicatorRepository = indicatorRepository;
    }

    @KafkaListener(topics = "indicator_events", groupId = "stockapi")
    public void listenGroupFoo(String message) throws JsonProcessingException {
        logger.info("Received Message in group stockapi: {}", message);
        String[] values = objectMapper.readValue(message, String[].class);

        UUID id = UUID.fromString(values[0]);

        Candle candle = candleService.getByIdWithPairAndIndicators(id);
        if(candle == null){ logger.info("Candle Not Found"); return; }
        Indicator macd = indicatorCalculator.calculateMACD(candle);
        indicatorRepository.save(macd);
        Indicator ema9 = indicatorCalculator.calculateEMA9(candle);
        indicatorRepository.save(ema9);
        Indicator macdSignal = indicatorCalculator.calculateMACDSignal(candle);
        indicatorRepository.save(macdSignal);
        logger.info("Saved Indicator Event in group stockapi");
    }
}
