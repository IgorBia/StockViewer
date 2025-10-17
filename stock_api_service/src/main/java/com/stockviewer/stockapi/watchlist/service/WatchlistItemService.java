package com.stockviewer.stockapi.watchlist.service;

import com.stockviewer.stockapi.candle.entity.Pair;
import com.stockviewer.stockapi.candle.repository.PairRepository;
import com.stockviewer.stockapi.watchlist.dto.WatchlistItemRequest;
import com.stockviewer.stockapi.watchlist.entity.Watchlist;
import com.stockviewer.stockapi.watchlist.entity.WatchlistItem;
import com.stockviewer.stockapi.watchlist.repository.WatchlistItemRepository;

import org.springframework.stereotype.Service;

@Service
public class WatchlistItemService {

    private final WatchlistItemRepository watchlistItemRepository;
    private final PairRepository pairRepository;
    private final WatchlistService watchlistService;

    public WatchlistItemService(WatchlistItemRepository watchlistItemRepository, PairRepository pairRepository, WatchlistService watchlistService) {
        this.watchlistItemRepository = watchlistItemRepository;
        this.pairRepository = pairRepository;
        this.watchlistService = watchlistService;
    }

    public WatchlistItem addWatchlistItem(WatchlistItemRequest watchlistItemRequest) {
        Watchlist watchlist = watchlistService.getWatchlistByName(watchlistItemRequest.getWatchlistName());
        Pair pair = pairRepository.findBySymbol(watchlistItemRequest.getSymbol());
        WatchlistItem watchlistItem = new WatchlistItem(pair, watchlist);

        return watchlistItemRepository.save(watchlistItem);
    }

    public void removeWatchlistItem(WatchlistItemRequest watchlistItemRequest) {
        Watchlist watchlist = watchlistService.getWatchlistByName(watchlistItemRequest.getWatchlistName());
        Pair pair = pairRepository.findBySymbol(watchlistItemRequest.getSymbol());

        watchlistItemRepository.deleteByPairIdAndWatchlistId(pair.getId(), watchlist.getId());
    }
}
