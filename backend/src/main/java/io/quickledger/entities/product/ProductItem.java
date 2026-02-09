package io.quickledger.entities.product;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.Company;
import jakarta.persistence.*;

import java.math.BigDecimal;
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
}
