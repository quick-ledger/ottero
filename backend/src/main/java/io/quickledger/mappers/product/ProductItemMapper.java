package io.quickledger.mappers.product;

import io.quickledger.dto.product.ProductItemDto;
import io.quickledger.entities.product.ProductItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

//@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Mapper(componentModel = "spring")
public interface ProductItemMapper {

    ProductItemMapper INSTANCE = Mappers.getMapper(ProductItemMapper.class);

    ProductItemDto toDto(ProductItem productItem);
    ProductItem toEntity(ProductItemDto productItemDto);
}