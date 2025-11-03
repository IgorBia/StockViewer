package com.stockviewer.stockapi.watchlist.controller;

import com.stockviewer.stockapi.watchlist.dto.WatchlistPatchRequest;
import com.stockviewer.stockapi.watchlist.dto.WatchlistRequest;
import com.stockviewer.stockapi.watchlist.dto.WatchlistResponse;
import com.stockviewer.stockapi.watchlist.entity.Watchlist;
import com.stockviewer.stockapi.watchlist.service.WatchlistService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping("/all")
    public List<WatchlistResponse> getWatchlist(){
        return watchlistService.getWatchlists();
    }

    @PostMapping
    public ResponseEntity<WatchlistResponse> addWatchlist(@Valid @RequestBody WatchlistRequest watchlistRequest){
        watchlistService.addWatchlist(watchlistRequest);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping
    public ResponseEntity<String> deleteWatchlist(@Valid @RequestBody WatchlistRequest watchlistRequest){
        watchlistService.removeWatchlist(watchlistRequest);
        return ResponseEntity.status(204).build();
    }

    @PatchMapping
    public ResponseEntity<String> updateWatchlist(@Valid @RequestBody WatchlistPatchRequest watchlistPatchRequest){
        return ResponseEntity.status(204).build();
    }
}
