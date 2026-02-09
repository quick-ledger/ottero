package io.quickledger.mappers.product;

import io.quickledger.dto.CompanyDto;
import io.quickledger.dto.product.ProductDefinitionDto;
import io.quickledger.entities.Company;
import io.quickledger.entities.product.ProductDefinition;
import io.quickledger.mappers.CompanyMapper;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper(uses = {CompanyMapper.class}, componentModel = "spring" ,unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductDefinitionMapper {
    ProductDefinitionMapper INSTANCE = Mappers.getMapper(ProductDefinitionMapper.class);

    @Mappings({
            @Mapping(source = "productAttributes", target = "productAttributes", qualifiedByName = "mapProductAttributes"),
            @Mapping(source = "company.id", target = "companyId")
    })
    ProductDefinitionDto toDto(ProductDefinition productDefinition);

    @Mappings({
            @Mapping(source = "productAttributes", target = "productAttributes", qualifiedByName = "mapProductAttributes"),
            @Mapping(source = "companyId", target = "company.id")
    })
    ProductDefinition toEntity(ProductDefinitionDto productDefinitionDTO);

    @Named("mapProductAttributes")
    default Map<String, Object> mapProductAttributes(List<Map<String, Object>> productAttributes) {
        Map<String, Object> flattenedMap = new HashMap<>();
        for (Map<String, Object> map : productAttributes) {
            flattenedMap.putAll(map);
        }
        return flattenedMap;
    }

    @Named("mapProductAttributes")
    default List<Map<String, Object>> mapProductAttributes(Map<String, Object> productAttributes) {
        List<Map<String, Object>> listOfMaps = new ArrayList<>();

        if (productAttributes == null) {
            productAttributes = new HashMap<>();
        }
        
        for (Map.Entry<String, Object> entry : productAttributes.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put(entry.getKey(), entry.getValue());
            listOfMaps.add(map);
        }
        return listOfMaps;
    }

    CompanyDto toDto(Company company);

    Company toEntity(CompanyDto companyDto);

    List<ProductDefinitionDto> toDtoList(List<ProductDefinition> productDefinitionList);

    List<ProductDefinition> toEntityList(List<ProductDefinitionDto> productDefinitionDtoList);

   /* @Mappings({
            @Mapping(source = "productAttributes", target = "productAttributes", qualifiedByName = "mapProductAttributes")
    })
    ProductDefinitionDto toDto(ProductDefinition productDefinition);

    @Mappings({
            @Mapping(source = "productAttributes", target = "productAttributes", qualifiedByName = "mapProductAttributes")
    })
    ProductDefinition toEntity(ProductDefinitionDto productDefinitionDTO);


    @Named("mapProductAttributes")
    default Map<String, Object> mapProductAttributes(List<Map<String, Object>> productAttributes) {
        Map<String, Object> flattenedMap = new HashMap<>();
        for (Map<String, Object> map : productAttributes) {
            flattenedMap.putAll(map);
        }
        return flattenedMap;
    }

    @Named("mapProductAttributes")
    default List<Map<String, Object>> mapProductAttributes(Map<String, Object> productAttributes) {
        List<Map<String, Object>> listOfMaps = new ArrayList<>();
        for (Map.Entry<String, Object> entry : productAttributes.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put(entry.getKey(), entry.getValue());
            listOfMaps.add(map);
        }
        return listOfMaps;
    }

    List<ProductDefinitionDto> toDtoList(List<ProductDefinition> productDefinitionList);

    List<ProductDefinition> toEntityList(List<ProductDefinitionDto> productDefinitionDtoList);*/
}