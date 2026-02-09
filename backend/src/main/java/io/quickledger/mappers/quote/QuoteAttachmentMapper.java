package io.quickledger.mappers.quote;

import io.quickledger.dto.quote.QuoteAttachmentDto;
import io.quickledger.entities.quote.QuoteAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuoteAttachmentMapper {
    @Mapping(target = "url", ignore = true) // URL will be set by service/controller if needed
    QuoteAttachmentDto toDto(QuoteAttachment attachment);
}
