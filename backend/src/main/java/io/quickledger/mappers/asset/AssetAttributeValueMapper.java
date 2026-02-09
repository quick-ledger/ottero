package io.quickledger.mappers.asset;

import io.quickledger.dto.asset.AssetAttributeValueDto;
import io.quickledger.entities.asset.AssetAttributeValue;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = AssetAttributeDefinitionMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetAttributeValueMapper {

    @Mapping(source = "value", target = "value")
    @Mapping(source = "assetDto", target = "asset")
    @Mapping(source = "definitionDto", target = "definition")
    //@Mapping(source = "selectableValueItemDto", target = "selectedValueItem")
    AssetAttributeValue toEntity(AssetAttributeValueDto assetAttributeValueDto);

    @InheritInverseConfiguration
    AssetAttributeValueDto toDto(AssetAttributeValue assetAttributeValue);
}