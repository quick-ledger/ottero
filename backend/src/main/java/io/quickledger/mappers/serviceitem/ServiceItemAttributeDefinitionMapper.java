package io.quickledger.mappers.serviceitem;

import io.quickledger.dto.serviceitem.ServiceItemAttributeDefinitionDto;
import io.quickledger.entities.serviceitem.ServiceItemAttributeDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceItemAttributeDefinitionMapper {

    @Mapping(source = "name", target = "name")
    ServiceItemAttributeDefinition toEntity(ServiceItemAttributeDefinitionDto serviceItemAttributeDefinitionDto);

    @Mapping(source = "name", target = "name")
    ServiceItemAttributeDefinitionDto toDto(ServiceItemAttributeDefinition serviceItemAttributeDefinition);
}