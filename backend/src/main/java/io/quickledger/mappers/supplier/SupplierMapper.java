package io.quickledger.mappers.supplier;

import io.quickledger.dto.supplier.SupplierDto;
import io.quickledger.entities.supplier.Supplier;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(source = "company.id", target = "companyId")
    SupplierDto toDto(Supplier supplier);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Supplier toEntity(SupplierDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDto(SupplierDto dto, @MappingTarget Supplier supplier);
}
