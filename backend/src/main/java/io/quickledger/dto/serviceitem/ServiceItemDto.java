package io.quickledger.dto.serviceitem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quickledger.entities.serviceitem.ServiceItem.Status;
import io.quickledger.entities.serviceitem.ServiceItem.Category;
import io.quickledger.entities.serviceitem.ServiceItem.SubCategory;

import java.math.BigDecimal;
import java.util.List;

public class ServiceItemDto {

    private Long id;
    //RG: I remove it from JSON response do you want it? I added this tag in other places as well! Mapper will drop them!
    @JsonIgnore
    private Long companyId;
    private Long assetId;
    private String name;
    private String itemDescription;
    private String code;
    private Category category;
    private SubCategory subCategory;
    private Status status;
    private Long quantity;
    //private Unit unit;
    private BigDecimal price;
    private BigDecimal item_tax;
    private BigDecimal discount;
    //TODO: in future need to handle image outside dto as it's binary object and not a good practice to keep it in dto
    @JsonIgnore
    private List<byte[]> images;
    private List<ServiceItemAttributeValueDto> serviceItemAttributeValues;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

//    public Unit getUnit() {
//        return unit;
//    }
//
//    public void setUnit(Unit unit) {
//        this.unit = unit;
//    }

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

    public List<ServiceItemAttributeValueDto> getServiceItemAttributeValues() {
        return serviceItemAttributeValues;
    }

    public void setServiceItemAttributeValues(List<ServiceItemAttributeValueDto> serviceItemAttributeValues) {
        this.serviceItemAttributeValues = serviceItemAttributeValues;
    }

    // ... (same as in ServiceItem, but with IDs instead of entity objects)


}