package io.quickledger.mappers.purchaseorder;

import io.quickledger.dto.purchaseorder.PurchaseOrderDto;
import io.quickledger.entities.purchaseorder.PurchaseOrder;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {PurchaseOrderItemMapper.class})
public interface PurchaseOrderMapper {

    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    PurchaseOrderDto toDto(PurchaseOrder entity);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    PurchaseOrder toEntity(PurchaseOrderDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDto(PurchaseOrderDto dto, @MappingTarget PurchaseOrder entity);
}
