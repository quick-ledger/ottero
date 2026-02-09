package io.quickledger.entities.product;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.Company;
import io.quickledger.services.product.JsonConverter;
import jakarta.persistence.*;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "product_definition")
public class ProductDefinition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "product_description")
    private String productDescription;

    @Convert(converter = JsonConverter.class)
    @Column(columnDefinition = "json", name = "product_attributes", nullable = true)
    private Map<String, Object> productAttributes; // JSON formatted string

    @OneToMany(mappedBy = "productDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductInstance> productInstances;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

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

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public Map<String, Object> getProductAttributes() {
        return productAttributes;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public void setProductAttributes(Map<String, Object> productAttributes) {
        this.productAttributes = productAttributes;
    }

    public List<ProductInstance> getProductInstances() {
        return productInstances;
    }

    public void setProductInstances(List<ProductInstance> productInstances) {
        this.productInstances = productInstances;
    }
}