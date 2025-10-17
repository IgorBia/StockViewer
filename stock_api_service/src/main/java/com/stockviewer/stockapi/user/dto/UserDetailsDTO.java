package com.stockviewer.stockapi.user.dto;

import com.stockviewer.stockapi.watchlist.dto.WatchlistResponse;

import java.util.Set;

public record UserDetailsDTO(
        String email,
        Set<WatchlistResponse> watchlists) {}
