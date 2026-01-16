package com.stockviewer.stockapi.candle.mapper;

import com.stockviewer.stockapi.candle.dto.PairDTO;
import com.stockviewer.stockapi.candle.entity.Pair;
import org.mapstruct.Mapper;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PairMapper {
    // Asset mapping handling
    PairDTO toDTO(Pair pair);
}