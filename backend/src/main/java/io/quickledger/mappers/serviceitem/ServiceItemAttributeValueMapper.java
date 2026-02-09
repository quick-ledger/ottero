package io.quickledger.mappers.serviceitem;

import io.quickledger.dto.serviceitem.ServiceItemAttributeValueDto;
import io.quickledger.entities.serviceitem.ServiceItemAttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ServiceItemAttributeDefinitionMapper.class)
public interface ServiceItemAttributeValueMapper {

    @Mapping(source = "serviceItemId", target = "serviceItem.id")
    @Mapping(source = "definition", target = "definition")
    ServiceItemAttributeValue toEntity(ServiceItemAttributeValueDto serviceItemAttributeValueDto);

    @Mapping(source = "definition", target = "definition")
    @Mapping(source = "serviceItem.id", target = "serviceItemId")
    ServiceItemAttributeValueDto toDto(ServiceItemAttributeValue serviceItemAttributeValue);


}