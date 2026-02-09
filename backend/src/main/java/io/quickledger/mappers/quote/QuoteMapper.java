package io.quickledger.mappers.quote;

import io.quickledger.dto.quote.QuoteDto;
import io.quickledger.entities.quote.Quote;
import org.mapstruct.*;

import java.util.Collections;
import java.util.stream.Collectors;

@Mapper(uses = { QuoteItemMapper.class, QuoteAttachmentMapper.class }, componentModel = "spring")
public interface QuoteMapper {

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "clientFirstname", source = "client.contactName")
    @Mapping(target = "clientLastname", source = "client.contactSurname")
    @Mapping(target = "clientEntityName", source = "client.entityName")
    @Mapping(target = "clientEmail", source = "client.email")
    @Mapping(target = "clientPhone", source = "client.phone")
    @Mapping(target = "discountValue", source = "discountValue")
    @Mapping(target = "discountType", source = "discountType")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "relatedInvoices", ignore = true) // Handled in AfterMapping
    QuoteDto toDto(Quote quote, @Context boolean lazyLoad);

    @Mapping(target = "client.id", source = "clientId")
    @Mapping(target = "user", ignore = true) // Handled manually in service
    @Mapping(source = "companyId", target = "company.id")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "itemDescription", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "invoices", ignore = true)
    Quote toEntity(QuoteDto quoteDto);

    @Mapping(target = "client.id", source = "clientId")
    @Mapping(target = "user", ignore = true) // Handled manually in service
    @Mapping(source = "companyId", target = "company.id")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "itemDescription", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "invoices", ignore = true)
    void updateEntityFromDto(QuoteDto quoteDto, @MappingTarget Quote quote);

    @AfterMapping
    default void afterMapping(Quote quote, @MappingTarget QuoteDto quoteDto, @Context boolean lazy) {
        if (lazy) {
            quoteDto.setQuoteItems(null);
        }
        if (quote.getInvoices() != null) {
            quoteDto.setRelatedInvoices(quote.getInvoices().stream()
                    .map(inv -> {
                        io.quickledger.dto.invoice.InvoiceDto dto = new io.quickledger.dto.invoice.InvoiceDto();
                        dto.setId(inv.getId());
                        dto.setInvoiceNumber(inv.getInvoiceNumber());
                        return dto;
                    })
                    .collect(Collectors.toList()));
        } else {
            quoteDto.setRelatedInvoices(Collections.emptyList());
        }
    }
}