package io.quickledger.entities.product;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.Company;
import jakarta.persistence.*;

/**
 * Defines an attribute type that can be assigned to ProductItem entities.
 * Examples: "length" (NUMBER, mm), "weight" (NUMBER, kg), "grade" (STRING), "color" (STRING)
 */
@Entity
@Table(name = "product_item_attribute_definitions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "company_id"}))
public class ProductItemAttributeDefinition extends BaseEntity {

    public enum DataType {
        STRING,
        NUMBER,
        DATE,
        BOOLEAN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 50)
    private DataType dataType = DataType.STRING;

    @Column(name = "unit", length = 50)
    private String unit; // e.g., "mm", "kg", "cm"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Getters and Setters

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

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
