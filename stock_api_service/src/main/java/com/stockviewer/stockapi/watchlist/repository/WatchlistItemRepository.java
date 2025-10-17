package com.stockviewer.stockapi.watchlist.repository;

import com.stockviewer.stockapi.watchlist.entity.WatchlistItem;
import com.stockviewer.stockapi.watchlist.entity.WatchlistItemId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, WatchlistItemId> {
    @Modifying
    @Transactional
    void deleteByPairIdAndWatchlistId(UUID pairId, UUID watchlistId);
}
