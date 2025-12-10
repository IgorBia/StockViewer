package com.stockviewer.stockapi.trade.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stockviewer.stockapi.trade.entity.Trade;
import com.stockviewer.stockapi.user.entity.User;

@Repository
public interface TradeRepository extends JpaRepository<Trade, UUID> {
    Optional<List<Trade>> findAllByUser(User user);
}
