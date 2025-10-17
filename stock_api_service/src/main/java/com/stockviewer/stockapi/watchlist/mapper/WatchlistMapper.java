package com.stockviewer.stockapi.watchlist.mapper;

import com.stockviewer.stockapi.watchlist.dto.WatchlistResponse;
import com.stockviewer.stockapi.watchlist.entity.Watchlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {WatchlistItemMapper.class})
public interface WatchlistMapper{

    @Mapping(target = "watchlistItems", source = "watchlistItems")
    WatchlistResponse toDTO(Watchlist watchlist);

    default List<WatchlistResponse> toDTOList(List<Watchlist> watchlists) {
        if (watchlists == null) return List.of();
        return watchlists.stream().map(this::toDTO).toList();
    }
}
