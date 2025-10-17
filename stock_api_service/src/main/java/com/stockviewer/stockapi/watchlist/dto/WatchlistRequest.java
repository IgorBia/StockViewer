package com.stockviewer.stockapi.watchlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class WatchlistRequest {
    @NotBlank(message = "Name of the watchlist cannot be empty")
    @Size(max = 15, message = "Name of the watchlist is too long")
    private String name;
}
