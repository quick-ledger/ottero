package io.quickledger.dto.product;

import java.math.BigDecimal;

public class ProductItemDto {
    private Long id;
    private String name;
    private String productDescription;
    private String code;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal item_tax;
    private BigDecimal discount;

    public ProductItemDto() {
    }

    public ProductItemDto(Long id, String name, String productDescription, String code, Integer quantity, BigDecimal price, BigDecimal item_tax, BigDecimal discount, String images) {
        this.id = id;
        this.name = name;
        this.productDescription = productDescription;
        this.code = code;
        this.quantity = quantity;
        this.price = price;
        this.item_tax = item_tax;
        this.discount = discount;
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
}
