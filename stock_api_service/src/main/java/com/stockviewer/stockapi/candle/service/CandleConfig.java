package com.stockviewer.stockapi.candle.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "candles")
@Setter
@Getter
public class CandleConfig {
    private List<String> supported;
}
