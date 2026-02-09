package io.quickledger.mappers;

import io.quickledger.dto.ClientDto;
import io.quickledger.entities.Client;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

//@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    @Mapping(source = "contactName", target = "firstName")
    @Mapping(source = "contactSurname", target = "lastName")
    @Mapping(source = "company.id", target = "companyId")
    ClientDto toDto(Client client);

    @InheritInverseConfiguration
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "itemDescription", ignore = true)
    @Mapping(target = "status", ignore = true)
    Client toEntity(ClientDto clientDto);
}