package com.stockviewer.stockapi.watchlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WatchlistItemRequest {

    @NotBlank(message = "Symbol cannot be empty")
    @Size(max = 10, message = "Symbol is too long")
    private String symbol;

    @NotBlank(message = "Watchlist name cannot be empty")
    private String watchlistName;
}
