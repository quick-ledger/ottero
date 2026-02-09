package io.quickledger.mappers;

import io.quickledger.dto.CompanyDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
    * This custom mappers helps to map the property "Map<String,Object> defaultCompany.productDefinitions[].productAttributes" to "List<Map<String,Object>> defaultCompany.productDefinitions[].productAttributes".
    *
 */
public class CustomMapper {

    public List<Map<String, Object>> map(Map<String, Object> value) {
        // Implement your mapping logic here
        return new ArrayList<>();
    }

    public Long map(CompanyDto value) {
        // Implement your mapping logic here
        return value.getId();
    }
}