package com.stockviewer.stockapi.watchlist.mapper;

import com.stockviewer.stockapi.watchlist.dto.WatchlistItemResponse;
import com.stockviewer.stockapi.watchlist.entity.WatchlistItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WatchlistItemMapper {

    @Mapping(target = "symbol", source = "pair.symbol")
    WatchlistItemResponse toDTO(WatchlistItem watchlistItem);

    default List<WatchlistItemResponse> toDtoList(List<WatchlistItem> watchlistItems) {
        if (watchlistItems == null || watchlistItems.isEmpty()) return List.of();
        return watchlistItems.stream().map(this::toDTO).toList();
    }
}
