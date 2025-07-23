package com.stockviewer.stockapi.user.mapper;

import com.stockviewer.stockapi.user.dto.UserDTO;
import com.stockviewer.stockapi.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserDTO dto);
}
