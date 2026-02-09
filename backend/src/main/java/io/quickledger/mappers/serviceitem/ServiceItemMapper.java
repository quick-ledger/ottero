package io.quickledger.mappers.serviceitem;

import io.quickledger.dto.serviceitem.ServiceItemAttributeValueDto;
import io.quickledger.dto.serviceitem.ServiceItemDto;
import io.quickledger.entities.serviceitem.ServiceItem;
import io.quickledger.entities.serviceitem.ServiceItemAttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ServiceItemMapper {

    //@Mapping(source = "asset.id", target = "assetId")
    @Mapping(source = "company.id", target = "companyId")
    ServiceItemDto toDto(ServiceItem serviceItem);

    @InheritInverseConfiguration
    ServiceItem toEntity(ServiceItemDto serviceItemDto);

    //ServiceItemAttributeValueDto toDto(ServiceItemAttributeValue serviceItemAttributeValue);

    //List<ServiceItemAttributeValueDto> toServiceItemAttributeValueDtoList(List<ServiceItemAttributeValue> serviceItemAttributeValues);

    List<ServiceItemDto> toServiceItemDtoList(List<ServiceItem> serviceItems);

    ServiceItemAttributeValue toEntity(ServiceItemAttributeValueDto serviceItemAttributeValueDto);
}