package io.quickledger.mappers.purchaseorder;

import io.quickledger.dto.purchaseorder.PurchaseOrderItemDto;
import io.quickledger.entities.purchaseorder.PurchaseOrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PurchaseOrderItemMapper {

    @Mapping(source = "productItem.id", target = "productItemId")
    @Mapping(source = "productItem.name", target = "productName")
    @Mapping(source = "productItem.code", target = "productCode")
    PurchaseOrderItemDto toDto(PurchaseOrderItem entity);

    @Mapping(target = "productItem", ignore = true)
    @Mapping(target = "purchaseOrder", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    PurchaseOrderItem toEntity(PurchaseOrderItemDto dto);
}
