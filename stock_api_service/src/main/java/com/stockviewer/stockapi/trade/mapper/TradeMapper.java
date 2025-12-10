package com.stockviewer.stockapi.trade.mapper;

import com.stockviewer.stockapi.trade.entity.Trade;
import com.stockviewer.stockapi.trade.dto.TradeDTO;

import java.util.List;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TradeMapper {

    @Mapping(target = "pairSymbol", source = "pair.symbol")
    TradeDTO toDTO(Trade trade);

    List<TradeDTO> toDTOList(List<Trade> trades);

}

