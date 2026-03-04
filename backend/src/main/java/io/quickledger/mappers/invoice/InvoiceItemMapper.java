package io.quickledger.mappers.invoice;

import io.quickledger.dto.invoice.InvoiceItemDto;
import io.quickledger.entities.invoice.InvoiceItem;
import io.quickledger.mappers.serviceitem.ServiceItemMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(uses = { ServiceItemMapper.class }, componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvoiceItemMapper {

    @Mapping(target = "invoiceId", source = "invoice.id")
    @Mapping(target = "serviceItemId", source = "serviceItem.id", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "productItemId", source = "productItem.id", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    InvoiceItemDto toDto(InvoiceItem invoiceItem);

    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "serviceItem.id", source = "serviceItemId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "productItem.id", source = "productItemId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    InvoiceItem toEntity(InvoiceItemDto invoiceItemDto);

    @AfterMapping
    default void afterMappingDtoToEntity(InvoiceItemDto dto, @MappingTarget InvoiceItem entity) {
        if (dto.getServiceItemId() == null) {
            entity.setServiceItem(null);
        }
        if (dto.getProductItemId() == null) {
            entity.setProductItem(null);
        }
    }
}
