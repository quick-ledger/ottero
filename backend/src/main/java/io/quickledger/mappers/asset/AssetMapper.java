package io.quickledger.mappers.asset;

import io.quickledger.dto.asset.AssetDto;
import io.quickledger.dto.asset.AssetGroupDto;
import io.quickledger.dto.asset.AssetAttributeValueDto;
import io.quickledger.entities.asset.Asset;
import io.quickledger.entities.asset.AssetAttributeValue;
import io.quickledger.entities.asset.AssetGroup;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = AssetAttributeDefinitionMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetMapper {

    @Mapping(source = "assetGroup.id", target = "assetGroupId")
    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "assetDescription", target = "description")
    AssetDto toDto(Asset asset);

    @InheritInverseConfiguration
    Asset toEntity(AssetDto assetDto);

    AssetAttributeValueDto toDto(AssetAttributeValue assetAttributeValue);

    AssetGroupDto toDto(AssetGroup assetGroup);

    AssetGroup toEntity(AssetGroupDto assetGroupDto);

    List<AssetDto> toAssetDtoList(List<Asset> assets); // Add this method

    List<AssetAttributeValueDto> toAssetAttributeValueDtoList(List<AssetAttributeValue> assetAttributeValues);

    AssetAttributeValue toEntity(AssetAttributeValueDto assetAttributeValueDto);
}