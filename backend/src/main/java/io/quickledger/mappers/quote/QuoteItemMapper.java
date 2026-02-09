package io.quickledger.mappers.quote;

import io.quickledger.dto.quote.QuoteItemDto;
import io.quickledger.entities.quote.QuoteItem;
import io.quickledger.mappers.product.ProductItemMapper;
import io.quickledger.mappers.serviceitem.ServiceItemMapper;
import org.mapstruct.*;

@Mapper(uses = { ProductItemMapper.class,
        ServiceItemMapper.class }, componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QuoteItemMapper {
    @Mapping(target = "quoteId", source = "quote.id")
    @Mapping(target = "productItemId", source = "productItem.id", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "serviceItemId", source = "serviceItem.id", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    QuoteItemDto toDto(QuoteItem quoteItem);

    @Mapping(target = "quote", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "serviceItem.id", source = "serviceItemId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "productItem.id", source = "productItemId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    QuoteItem toEntity(QuoteItemDto quoteItemDto);

    @Mapping(target = "quote", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "serviceItem.id", source = "serviceItemId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "productItem.id", source = "productItemId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(QuoteItemDto dto, @MappingTarget QuoteItem entity);

    @AfterMapping
    default void afterMappingDtoToEntity(QuoteItemDto dto, @MappingTarget QuoteItem entity) {
        if (dto.getServiceItemId() == null)
            entity.setServiceItem(null);

        if (dto.getProductItemId() == null)
            entity.setProductItem(null);

    }
}