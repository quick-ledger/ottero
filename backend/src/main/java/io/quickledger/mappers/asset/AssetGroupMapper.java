package io.quickledger.mappers.asset;

import io.quickledger.dto.asset.AssetGroupDto;
import io.quickledger.entities.asset.AssetGroup;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetGroupMapper {

    @Mapping(source = "company.id", target = "companyId")
    AssetGroupDto toDto(AssetGroup assetGroup);

    @InheritInverseConfiguration
    AssetGroup toEntity(AssetGroupDto assetGroupDto);
}