package com.stockviewer.stockapi.watchlist.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WatchlistPatchRequest {
    @NotBlank(message = "Old name of the watchlist cannot be empty")
    @Size(max = 15, message = "Old name of the watchlist is too long")
    private String oldName;

    @NotBlank(message = "New name of the watchlist cannot be empty")
    @Size(max = 15, message = "New name of the watchlist is too long")
    private String newName;
}
