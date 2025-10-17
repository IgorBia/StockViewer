package com.stockviewer.stockapi.watchlist.repository;

import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.watchlist.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


import java.util.UUID;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, UUID> {
    List<Watchlist> findByUser(User user);
    Optional<Watchlist> findByNameAndUser(String watchlistName, User user);
    boolean existsByNameAndUser(String watchlistName, User user);

    User user(User user);
}
