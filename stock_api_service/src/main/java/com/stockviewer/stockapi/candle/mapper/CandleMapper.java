package com.stockviewer.stockapi.candle.mapper;

import com.stockviewer.stockapi.candle.dto.CandleDTO;
import com.stockviewer.stockapi.candle.entity.Candle;
import com.stockviewer.stockapi.candle.entity.Indicator;
import com.stockviewer.stockapi.indicators.IndicatorDTO;
import org.mapstruct.Mapper;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CandleMapper {
    @Mapping(target = "indicators", source = "indicators", qualifiedByName = "mapIndicators")
    CandleDTO toDTO(Candle candle);

    @Named("mapIndicators")
    default List<IndicatorDTO> mapIndicators(List<Indicator> indicators) {
        if (indicators == null) return List.of();
        return indicators.stream()
                .map(ind -> new IndicatorDTO(
                        ind.getIndicatorId().getName(),
                        ind.getValue()
                ))
                .toList();
    }

    @BeforeMapping
    default void checkNull(Candle candle) {
        if (candle == null) {
            throw new IllegalArgumentException("Candle cannot be null.");
        }
    }
}