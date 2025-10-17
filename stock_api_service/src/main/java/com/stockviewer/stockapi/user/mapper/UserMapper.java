package com.stockviewer.stockapi.user.mapper;

import com.stockviewer.stockapi.user.dto.UserDTO;
import com.stockviewer.stockapi.user.dto.UserDetailsDTO;
import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.watchlist.mapper.WatchlistMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {WatchlistMapper.class})
public interface UserMapper {
    User toEntity(UserDTO dto);

    @Mapping(target = "watchlists", source = "watchlists")
    UserDetailsDTO toDTO(User user);
}
