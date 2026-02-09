package io.quickledger.dto.product;

import java.util.Map;

public class ProductInstanceDto {
    private Long id;
    private String name;
    private String instanceDescription;
    private Map<String, Object> instanceAttributes;
    private Long productDefinitionId;

    // Standard getters and setters

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

    public String getInstanceDescription() {
        return instanceDescription;
    }

    public void setInstanceDescription(String instanceDescription) {
        this.instanceDescription = instanceDescription;
    }

    public Map<String, Object> getInstanceAttributes() {
        return instanceAttributes;
    }

    public void setInstanceAttributes(Map<String, Object> instanceAttributes) {
        this.instanceAttributes = instanceAttributes;
    }

    public Long getProductDefinitionId() {
        return productDefinitionId;
    }

    public void setProductDefinitionId(Long productDefinitionId) {
        this.productDefinitionId = productDefinitionId;
    }
}