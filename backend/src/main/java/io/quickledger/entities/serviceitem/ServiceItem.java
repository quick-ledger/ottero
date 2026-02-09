package io.quickledger.entities.serviceitem;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.Company;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;

//this is a simplified version of service similar to ProductItem. It has no dependencies to asset or attributes.

@Entity
@Table(name = "service_items") // This names the table as "service_items" in the database
public class ServiceItem extends BaseEntity {

    //Default constructor to avoid NPE
    public ServiceItem() {
        this.serviceItemAttributeValues = new ArrayList<>(); // Initialize the list
    }

    //Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "id", nullable = false) // Company that owns the service item
    private Company company;

    //@ManyToOne
    //@JoinColumn(name ="asset_id", nullable = true)
    //private Asset asset;

    @Column(name = "name", length = 500, nullable = true) // General name or identifier
    private String name;

    @Column(name = "item_description", length = 1000, nullable = true)
    private String itemDescription;

    @Column(name = "code", length = 500, nullable = true) // Specific code (Barcode or QBR code)for the service item
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 500, nullable = true) // Category of the service item
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category", length = 500, nullable = true) // Sub-category of the service item
    private SubCategory subCategory;

    @Column(name = "quantity", length = 6, nullable = true) // Unit of the service item
    private Long quantity;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "unit", length = 500, nullable = true) // Unit of the service item
//    private Unit unit;

    @Column(name = "price", nullable = true, scale =2 ,  precision = 20) // Price of the service item
    private BigDecimal price;

    @Column(name = "item_tax", nullable = true,scale =2 ,  precision = 20) // Tax of the service item
    private BigDecimal item_tax;

    @Column(name = "discount", nullable = true, scale =2 ,  precision = 20) // Discount of the service item
    private BigDecimal discount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 500, nullable = true) // Status of the service item
    private Status status;

    @ElementCollection
    @Column(name = "images", nullable = true, columnDefinition="MEDIUMBLOB")
    private List<byte[]> images;

    /* INFO: using assetAttributeValues to store any new attributes of the asset using EVA (Entity-Value-Attribute) model
        This is a one-to-many relationship with AssetAttributeValue and has a cascade type of ALL and orphanRemoval set to true
        TODO: MBH: This means that if an asset is removed, all its attribute values will be removed as well which needs to be tested
        @OneToMany(mappedBy = "serviceItem", cascade = CascadeType.ALL, orphanRemoval = true)
     */
    @OneToMany(mappedBy = "serviceItem")
    private List<ServiceItemAttributeValue> serviceItemAttributeValues;

    // Getters and Setters

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

//    public Asset getAsset() {
//        return asset;
//    }
//
//    public void setAsset(Asset asset) {
//        this.asset = asset;
//    }

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

    public List<ServiceItemAttributeValue> getServiceItemAttributeValues() {
        return serviceItemAttributeValues;
    }

    public void setServiceItemAttributeValues(List<ServiceItemAttributeValue> serviceItemAttributeValues) {
        this.serviceItemAttributeValues = serviceItemAttributeValues;
    }

    //toString method
    @Override
    public String toString() {
        return "ServiceItem{" +
                "id=" + id + '\'' +
                ", company=" + company + '\'' +
                //", asset=" + asset + '\'' +
                ", name='" + name + '\'' +
                ", itemDescription='" + itemDescription + '\'' +
                ", code='" + code + '\'' +
                ", category='" + category + '\'' +
                ", subCategory='" + subCategory + '\'' +
                ", quantity=" + quantity + '\'' +
                ", price=" + price + '\'' +
                ", item_tax=" + item_tax + '\'' +
                ", discount=" + discount + '\'' +
                ", status='" + status + '\'' +
                ", baseEntity=" + super.toString() + '\'' +
//                ", images=" + images +
                '}';
    }


    public enum Status {
        ACTIVE, INACTIVE, DISPOSED
    }
    public enum Category {
        TANGIBLE, INTANGIBLE
    }
    public enum SubCategory {
        LAND, BUILDING, VEHICLE, EQUIPMENT, FURNITURE, COMPUTER, SOFTWARE, PATENT, COPYRIGHT, TRADEMARK
    }
//    public enum Unit {
//        PIECES, KILOGRAMS, LITERS, METERS, SQUARE_METERS, CUBIC_METERS, HOURS, KILOMETERS, LITERS_PER_KILOMETER, KILOMETERS_PER_HOUR
//    }
}
