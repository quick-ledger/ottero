package io.quickledger.mappers.invoice;

import io.quickledger.dto.invoice.InvoiceDto;
import io.quickledger.entities.invoice.Invoice;
import org.mapstruct.*;

@Mapper(uses = { InvoiceItemMapper.class }, componentModel = "spring")
public interface InvoiceMapper {

    @InheritInverseConfiguration
    @Mapping(target = "clientFirstname", source = "client.contactName")
    @Mapping(target = "clientLastname", source = "client.contactSurname")
    @Mapping(target = "clientEntityName", source = "client.entityName")
    @Mapping(target = "clientEmail", source = "client.email")
    @Mapping(target = "clientPhone", source = "client.phone")
    @Mapping(target = "discountValue", source = "discountValue")
    @Mapping(target = "discountType", source = "discountType")
    @Mapping(target = "quoteId", source = "quote.id")
    @Mapping(target = "quoteNumber", source = "quote.quoteNumber")
    @Mapping(target = "paymentLink", ignore = true)
    InvoiceDto toDto(Invoice invoice, @Context boolean lazyLoad);

    @Mapping(target = "client.id", source = "clientId")
    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "invoiceItems", source = "invoiceItems")
    @Mapping(source = "companyId", target = "company.id")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "itemDescription", ignore = true)
    @Mapping(target = "quote", ignore = true)
    @Mapping(target = "employee", ignore = true)
    Invoice toEntity(InvoiceDto invoiceDto);

    @AfterMapping
    default void afterMapping(@MappingTarget InvoiceDto invoiceDto, @Context boolean lazy) {
        if (lazy)
            invoiceDto.setInvoiceItems(null);
    }

}
