package io.quickledger.entities.product;

import io.quickledger.entities.BaseEntity;
import jakarta.persistence.*;

/**
 * Stores the value of a specific attribute for a ProductItem.
 * Part of the EVA (Entity-Value-Attribute) pattern for flexible product attributes.
 */
@Entity
@Table(name = "product_item_attribute_values",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_item_id", "attribute_definition_id"}))
public class ProductItemAttributeValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "value", nullable = false, length = 1000)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    private ProductItemAttributeDefinition definition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_item_id", nullable = false)
    private ProductItem productItem;

    // Getters and Setters

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

    public ProductItemAttributeDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(ProductItemAttributeDefinition definition) {
        this.definition = definition;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }
}
