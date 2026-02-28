package io.quickledger.mappers.expense;

import io.quickledger.dto.expense.ExpenseDto;
import io.quickledger.entities.expense.Expense;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {ExpenseAttachmentMapper.class})
public interface ExpenseMapper {

    @Mapping(source = "company.id", target = "companyId")
    ExpenseDto toDto(Expense expense);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Expense toEntity(ExpenseDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDto(ExpenseDto dto, @MappingTarget Expense expense);
}
