package io.quickledger.entities.product;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.Company;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a bare minimum product (similar to ServiceItem) to get started with in the quote UI.
 * No attributes please!
 */
@Entity
@Table(name = "product_items")
public class ProductItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "id", nullable = false)
    private Company company;

    @Column(name = "name", length = 500, nullable = true) // General name or identifier
    private String name;

    @Column(name = "product_description", length = 1000, nullable = true)
    private String productDescription;

    @Column(name = "code", length = 500, nullable = true)
    private String code;

    @Column(name = "quantity", length = 6, nullable = true)
    private Integer quantity;

    @Column(name = "price", nullable = true, scale =2 ,  precision = 20)
    private BigDecimal price;

    @Column(name = "item_tax", nullable = true,scale =2 ,  precision = 20)
    private BigDecimal item_tax;

    @Column(name = "discount", nullable = true, scale =2 ,  precision = 20)
    private BigDecimal discount;

    @ElementCollection
    @Column(name = "images", nullable = true, columnDefinition="MEDIUMBLOB")
    private List<byte[]> images;

    // Inventory tracking fields
    @Column(name = "quantity_on_hand", nullable = false, columnDefinition = "integer default 0")
    private Integer quantityOnHand = 0;

    @Column(name = "reorder_point")
    private Integer reorderPoint;

    @Column(name = "reorder_quantity")
    private Integer reorderQuantity;

    @Column(name = "track_inventory", nullable = false, columnDefinition = "boolean default false")
    private Boolean trackInventory = false;

    // EVA pattern for flexible attributes
    @OneToMany(mappedBy = "productItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductItemAttributeValue> attributeValues = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getItem_tax() {
        return item_tax;
    }

    public void setItem_tax(BigDecimal item_tax) {
        this.item_tax = item_tax;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public List<byte[]> getImages() {
        return images;
    }

    public void setImages(List<byte[]> images) {
        this.images = images;
    }

    public Integer getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(Integer quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public Integer getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public Boolean getTrackInventory() {
        return trackInventory;
    }

    public void setTrackInventory(Boolean trackInventory) {
        this.trackInventory = trackInventory;
    }

    public List<ProductItemAttributeValue> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(List<ProductItemAttributeValue> attributeValues) {
        this.attributeValues = attributeValues;
    }
}
