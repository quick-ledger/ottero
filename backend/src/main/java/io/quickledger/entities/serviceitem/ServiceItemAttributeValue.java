package io.quickledger.entities.serviceitem;

import jakarta.persistence.*;

@Entity
@Table(name = "service_item_attribute_values")
public class ServiceItemAttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String value;

    // TODO: If it was unique then must not have cascade or orphanRemoval since definition might be related to other values, value removal must not remove definition
    // RG: it depends on how you handle it in GUI, for now This logic works, but you need to group them based on definitions for your columns
    //    @ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    private ServiceItemAttributeDefinition definition;

    @ManyToOne
    @JoinColumn(name = "service_item_id", nullable = false)
    private ServiceItem serviceItem;

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

    public ServiceItemAttributeDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(ServiceItemAttributeDefinition definition) {
        this.definition = definition;
    }

    public ServiceItem getServiceItem() {
        return serviceItem;
    }

    public void setServiceItem(ServiceItem serviceItem) {
        this.serviceItem = serviceItem;
    }

}