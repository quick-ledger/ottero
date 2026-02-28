package io.quickledger.mappers.expense;

import io.quickledger.dto.expense.ExpenseAttachmentDto;
import io.quickledger.entities.expense.ExpenseAttachment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpenseAttachmentMapper {

    ExpenseAttachmentDto toDto(ExpenseAttachment attachment);
}
