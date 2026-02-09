package io.quickledger.dto.product;

import io.quickledger.dto.BaseEntityDto;

import java.util.List;
import java.util.Map;

public class ProductDefinitionDto extends BaseEntityDto {

    private Long id;
    private String name;
    private String productDescription;
    private List<Map<String, Object>> productAttributes;
    private List<ProductDefinitionDto> productDefinitions;
    private Long companyId;
    // Standard getters and setters


    @Override
    public String toString() {
        return "ProductDefinitionDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", productDescription='" + productDescription + '\'' +
                ", productAttributes=" + productAttributes +
                ", companyId=" + companyId +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public List<Map<String, Object>> getProductAttributes() {
        return productAttributes;
    }

    public void setProductAttributes(List<Map<String, Object>> productAttributes) {
        this.productAttributes = productAttributes;
    }

    public List<ProductDefinitionDto> getProductDefinitions() {
        return productDefinitions;
    }

    public void setProductDefinitions(List<ProductDefinitionDto> productDefinitions) {
        this.productDefinitions = productDefinitions;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}
