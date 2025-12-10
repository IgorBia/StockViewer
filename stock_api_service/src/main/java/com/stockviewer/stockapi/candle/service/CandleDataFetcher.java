package com.stockviewer.stockapi.candle.service;

import com.stockviewer.stockapi.candle.dto.CandleDTO;
import com.stockviewer.stockapi.candle.repository.CandleRepository;
import java.math.BigDecimal;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CandleDataFetcher {

    public CandleDataFetcher() {
    }

    public CandleDTO fetchCandleData(String symbol, String timeframe) {


        String url = "https://api.binance.com/api/v3/klines?symbol=SYMBOL&interval=INTERVAL&limit=1"
                .replace("SYMBOL", symbol)
                .replace("INTERVAL", timeframe);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch candle data", e);
        }

        CandleDTO candleDTO = new CandleParser().parseDTO(response.body());

        return candleDTO;
    }

    public BigDecimal fetchTicker(String symbol) {
        String url = "https://api.binance.com/api/v3/ticker/price?symbol=SYMBOL"
                .replace("SYMBOL", symbol);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch ticker data", e);
        }

        return new CandleParser().parseTicker(response.body());
    }
}