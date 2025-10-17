package com.stockviewer.stockapi.watchlist.controller;

import com.stockviewer.stockapi.watchlist.dto.WatchlistItemRequest;
import com.stockviewer.stockapi.watchlist.entity.WatchlistItem;
import com.stockviewer.stockapi.watchlist.service.WatchlistItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/watchlist/item")
public class WatchlistItemController {

    private final WatchlistItemService watchlistItemService;

    public WatchlistItemController(WatchlistItemService watchlistItemService) {
        this.watchlistItemService = watchlistItemService;
    }

    @PostMapping
    public ResponseEntity<String> addWatchlistItem(@Valid @RequestBody WatchlistItemRequest watchlistItemRequest){
        watchlistItemService.addWatchlistItem(watchlistItemRequest);
        return ResponseEntity.status(204).build();
    }

    @DeleteMapping
    public ResponseEntity<String> deleteWatchlistItem(@Valid @RequestBody WatchlistItemRequest watchlistItemRequest){
        watchlistItemService.removeWatchlistItem(watchlistItemRequest);
        return ResponseEntity.status(204).build();
    }
}
