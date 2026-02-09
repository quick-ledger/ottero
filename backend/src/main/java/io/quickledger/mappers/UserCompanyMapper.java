package io.quickledger.mappers;

import io.quickledger.dto.UserCompanyDto;
import io.quickledger.entities.UserCompany;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper (unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserCompanyMapper {
    UserCompanyMapper INSTANCE = Mappers.getMapper(UserCompanyMapper.class);

    UserCompanyDto toDto(UserCompany userCompany);

    UserCompany toEntity(UserCompanyDto userCompanyDto);
}