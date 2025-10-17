package com.stockviewer.stockapi.watchlist.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stockviewer.stockapi.candle.entity.Pair;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(schema="user_management", name="watchlist_item")
public class WatchlistItem {

    public WatchlistItem(Pair pair, Watchlist watchlist) {
        this.pair = pair;
        this.watchlist = watchlist;
        this.watchlistItemId = new WatchlistItemId();
        this.watchlistItemId.setPairId(pair.getId());
        this.watchlistItemId.setWatchlistId(watchlist.getId());
    }

    @JsonIgnore
    @EmbeddedId
    private WatchlistItemId watchlistItemId;

    @JsonIgnore
    @ManyToOne
    @MapsId("watchlistId")
    @JoinColumn(name = "watchlist_id")
    private Watchlist watchlist;

    @ManyToOne
    @MapsId("pairId")
    @JoinColumn(name = "pair_id")
    private Pair pair;
}
