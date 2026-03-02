package io.quickledger.mappers.product;

import io.quickledger.dto.product.ProductItemAttributeDefinitionDto;
import io.quickledger.entities.product.ProductItemAttributeDefinition;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductItemAttributeDefinitionMapper {

    @Mapping(source = "company.id", target = "companyId")
    ProductItemAttributeDefinitionDto toDto(ProductItemAttributeDefinition entity);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    ProductItemAttributeDefinition toEntity(ProductItemAttributeDefinitionDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDto(ProductItemAttributeDefinitionDto dto, @MappingTarget ProductItemAttributeDefinition entity);
}
