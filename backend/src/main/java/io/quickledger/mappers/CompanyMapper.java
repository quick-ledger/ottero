package io.quickledger.mappers;

import io.quickledger.dto.CompanyDto;
import io.quickledger.entities.Company;
import io.quickledger.mappers.product.ProductDefinitionMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Mapping;

@Mapper(uses = {ProductDefinitionMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyMapper {

    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

//    @Mapping(source = "productDefinitions", target = "productDefinitions")
//    @Mapping(source = "id", target = "companyId")
    CompanyDto toDto(Company company);

//    @Mapping(source = "productDefinitions", target = "productDefinitions")
//    @Mapping(source = "companyId", target = "id")

    @Mapping(ignore = true, target = "image")
    Company toEntity(CompanyDto companyDto);

    /*List<ProductDefinitionDto> toDto(List<ProductDefinition> productDefinitions);

    List<ProductDefinition> toEntity(List<ProductDefinitionDto> productDefinitionDtos);*/
}