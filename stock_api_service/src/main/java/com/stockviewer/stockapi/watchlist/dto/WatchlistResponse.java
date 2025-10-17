package com.stockviewer.stockapi.watchlist.dto;

import java.util.Set;

public record WatchlistResponse(String name, Set<WatchlistItemResponse> watchlistItems) {
}
