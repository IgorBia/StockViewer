package com.stockviewer.stockapi.mapper;

import com.stockviewer.stockapi.dto.UserDTO;
import com.stockviewer.stockapi.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserDTO dto);
}
