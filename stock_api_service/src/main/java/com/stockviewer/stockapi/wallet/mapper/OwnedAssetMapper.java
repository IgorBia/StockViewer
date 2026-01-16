package com.stockviewer.stockapi.wallet.mapper;

import com.stockviewer.stockapi.wallet.dto.OwnedAssetDTO;
import com.stockviewer.stockapi.wallet.entity.OwnedAsset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OwnedAssetMapper {

    @Mapping(target = "name", source = "asset.symbol")
    OwnedAssetDTO toDTO(OwnedAsset ownedAsset);

    default List<OwnedAssetDTO> toDtoList(List<OwnedAsset> ownedAssets) {
        if (ownedAssets == null || ownedAssets.isEmpty()) return List.of();
        return ownedAssets.stream().map(this::toDTO).toList();
    }
}
