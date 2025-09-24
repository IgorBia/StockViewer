package com.stockviewer.stockapi.indicators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class IndicatorListener {

    private final static Logger logger = LoggerFactory.getLogger(IndicatorListener.class);

    @KafkaListener(topics = "indicator_events", groupId = "stockapi")
    public void listenGroupFoo(String message) {
        logger.info("Received Message in group stockapi: {}", message);
    }
}
