package io.quickledger.dto.serviceitem;

public class ServiceItemAttributeValueDto {
    private Long id;
    private String value;
    private Long serviceItemId;
    private ServiceItemAttributeDefinitionDto definition;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getServiceItemId() {
        return serviceItemId;
    }

    public void setServiceItemId(Long serviceItemId) {
        this.serviceItemId = serviceItemId;
    }

    public ServiceItemAttributeDefinitionDto getDefinition() {
        return definition;
    }

    public void setDefinition(ServiceItemAttributeDefinitionDto definition) {
        this.definition = definition;
    }
}
