package com.stockviewer.stockapi.watchlist.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistItemId implements Serializable {

    @JsonIgnore
    private UUID watchlistId;
    @JsonIgnore
    private UUID pairId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WatchlistItemId that = (WatchlistItemId) o;
        return Objects.equals(watchlistId, that.watchlistId) && Objects.equals(pairId, that.pairId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(watchlistId, pairId);
    }
}