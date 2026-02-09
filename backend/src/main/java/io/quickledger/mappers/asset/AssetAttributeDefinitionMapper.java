package io.quickledger.mappers.asset;

import io.quickledger.dto.asset.AssetAttributeDefinitionDto;
import io.quickledger.entities.asset.AssetAttributeDefinition;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetAttributeDefinitionMapper {

    @Mapping(source = "assetGroup", target = "assetGroup")
    AssetAttributeDefinitionDto toDto(AssetAttributeDefinition assetAttributeDefinition);

    @InheritInverseConfiguration
    AssetAttributeDefinition toEntity(AssetAttributeDefinitionDto assetAttributeDefinitionDto);
}