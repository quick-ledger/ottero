package io.quickledger.mappers.inventory;

import io.quickledger.dto.inventory.StockMovementDto;
import io.quickledger.entities.inventory.StockMovement;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {

    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "productItem.id", target = "productItemId")
    @Mapping(source = "productItem.name", target = "productName")
    @Mapping(source = "productItem.code", target = "productCode")
    StockMovementDto toDto(StockMovement entity);
}
