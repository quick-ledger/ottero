package io.quickledger.mappers.product;

import io.quickledger.dto.product.ProductInstanceDto;
import io.quickledger.entities.product.ProductInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductInstanceMapper {
    ProductInstanceMapper INSTANCE = Mappers.getMapper(ProductInstanceMapper.class);

    @Mapping(source = "productDefinition.id", target = "productDefinitionId")
    ProductInstanceDto toDto(ProductInstance productInstance);

    @Mapping(source = "productDefinitionId", target = "productDefinition.id")
    ProductInstance toEntity(ProductInstanceDto productInstanceDto);
}