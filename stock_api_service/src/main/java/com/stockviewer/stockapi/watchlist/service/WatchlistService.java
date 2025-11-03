package com.stockviewer.stockapi.watchlist.service;

import com.stockviewer.stockapi.exception.DuplicateException;
import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.user.service.UserService;
import com.stockviewer.stockapi.watchlist.dto.WatchlistPatchRequest;
import com.stockviewer.stockapi.watchlist.dto.WatchlistRequest;
import com.stockviewer.stockapi.watchlist.dto.WatchlistResponse;
import com.stockviewer.stockapi.watchlist.entity.Watchlist;
import com.stockviewer.stockapi.watchlist.mapper.WatchlistMapper;
import com.stockviewer.stockapi.watchlist.repository.WatchlistRepository;

import jakarta.transaction.Transactional;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WatchlistService {
    private final WatchlistRepository watchlistRepository;
    private final UserService userService;
    private final WatchlistMapper watchlistMapper;

    private static final Logger logger =LoggerFactory.getLogger(WatchlistService.class);

    public WatchlistService(WatchlistRepository watchlistRepository, UserService userService, WatchlistMapper watchlistMapper) {
        this.watchlistRepository = watchlistRepository;
        this.userService = userService;
        this.watchlistMapper = watchlistMapper;
    }

    public List<WatchlistResponse> getWatchlists() {
        return watchlistMapper.toDTOList(
                watchlistRepository.findByUser(userService.getUserFromContext())
        );
    }

    public WatchlistResponse addWatchlist(WatchlistRequest watchlistRequest) {
        User user = userService.getUserFromContext();
        Watchlist watchlist = watchlistRepository.save(new Watchlist(user, watchlistRequest.getName()));
        return watchlistMapper.toDTO(watchlist);
    }

    public void removeWatchlist(WatchlistRequest watchlistRequest) {
        watchlistRepository.delete(getWatchlistByName(watchlistRequest.getName()));
    }

    @Transactional
    public WatchlistResponse patchWatchlist(WatchlistPatchRequest watchlistPatchRequest) {

        if (watchlistPatchRequest.getOldName().equals(watchlistPatchRequest.getNewName())) {
            throw new DuplicateException("Old name and new name is the same");
        }

        if (watchlistRepository.existsByNameAndUser(watchlistPatchRequest.getNewName(), userService.getUserFromContext())) {
            throw new DuplicateException("Watchlist with this name already exists");
        }

        Watchlist watchlist = getWatchlistByName(watchlistPatchRequest.getOldName());

        watchlist.setName(watchlistPatchRequest.getNewName());

        return watchlistMapper.toDTO(watchlist);
    }

    protected Watchlist getWatchlistByName(String watchlistName) {
        User user = userService.getUserFromContext();
        return watchlistRepository
                .findByNameAndUser(watchlistName, user)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found for name: " + watchlistName + " and user: " +  user.getUserId()));
    }
}
