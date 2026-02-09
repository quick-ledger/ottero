package io.quickledger.mappers.invoice;

import io.quickledger.dto.invoice.InvoiceItemDto;
import io.quickledger.entities.invoice.InvoiceItem;
import io.quickledger.mappers.serviceitem.ServiceItemMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = { ServiceItemMapper.class }, componentModel = "spring")
public interface InvoiceItemMapper {

    @Mapping(target = "invoiceId", source = "invoice.id")
    @Mapping(target = "serviceItemId", source = "serviceItem.id")
    @Mapping(target = "productItemId", source = "productItem.id")
    InvoiceItemDto toDto(InvoiceItem invoiceItem);

    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "serviceItem.id", source = "serviceItemId")
    @Mapping(target = "productItem.id", source = "productItemId")
    InvoiceItem toEntity(InvoiceItemDto invoiceItemDto);
}
