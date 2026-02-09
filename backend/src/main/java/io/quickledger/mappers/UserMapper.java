package io.quickledger.mappers;

import io.quickledger.dto.CompanyDto;
import io.quickledger.dto.UserDto;
import io.quickledger.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper(uses = {CompanyMapper.class, CustomMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    //@Mapping(source= "defaultCompany.id", target = "defaultCompanyId")
    @Mapping(source= "defaultCompany", target = "defaultCompany")
    UserDto toDto(User user);

    @Mapping(source= "defaultCompany", target = "defaultCompany")
    User toEntity(UserDto userDto);

    List<UserDto> toDtoList(List<User> users);

}