package io.quickledger.entities.product;

import io.quickledger.entities.BaseEntity;
import io.quickledger.services.product.JsonConverter;
import jakarta.persistence.*;

import java.util.Map;

@Entity
@Table(name = "product_instance")
public class ProductInstance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "instance_description")
    private String instanceDescription;

    @Convert(converter = JsonConverter.class)
    @Column(columnDefinition = "json", name = "instance_attributes")
    private Map<String, Object> instanceAttributes; // JSON formatted string

    @ManyToOne
    @JoinColumn(name = "product_definition_id", nullable = false)
    private ProductDefinition productDefinition;

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

    public ProductDefinition getProductDefinition() {
        return productDefinition;
    }

    public void setProductDefinition(ProductDefinition productDefinition) {
        this.productDefinition = productDefinition;
    }
}
