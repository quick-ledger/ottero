package io.quickledger.mappers.product;

import io.quickledger.dto.product.ProductItemAttributeValueDto;
import io.quickledger.entities.product.ProductItemAttributeValue;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductItemAttributeValueMapper {

    @Mapping(source = "productItem.id", target = "productItemId")
    @Mapping(source = "definition.id", target = "definitionId")
    @Mapping(source = "definition.name", target = "definitionName")
    @Mapping(source = "definition.dataType", target = "dataType")
    @Mapping(source = "definition.unit", target = "unit")
    ProductItemAttributeValueDto toDto(ProductItemAttributeValue entity);

    @Mapping(target = "productItem", ignore = true)
    @Mapping(target = "definition", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    ProductItemAttributeValue toEntity(ProductItemAttributeValueDto dto);
}
