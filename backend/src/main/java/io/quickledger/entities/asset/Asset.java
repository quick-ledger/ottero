package io.quickledger.entities.asset;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.Company;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "assets") // This names the table as "assets" in the database

/**
 * asset instance. this is the actual asset. relates to asset attribute values.
 */
public class Asset extends BaseEntity {
    //default constructor to avoid NPE
    public Asset() {
        this.assetAttributeValues = new ArrayList<>(); // Initialize the list
    }

    // do not put too many fields here. fields should be attributes on the asset definition. consider these as system fields.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Column(name = "asset_description", length = 1000, nullable = true)
    private String assetDescription;

    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = true)
    private AssetGroup assetGroup;


    @Column(name = "quantity", nullable = true)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 100, nullable = true, columnDefinition = "varchar(100) default 'ACTIVE'")
    private Status status;

    // Asset identification
    @Column(name = "code", length = 100)
    private String code;

    @Column(name = "serial_number", length = 200)
    private String serialNumber;

    @Column(name = "location", length = 500)
    private String location;

    // Financial tracking
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_price", precision = 19, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "current_value", precision = 19, scale = 2)
    private BigDecimal currentValue;

    // Depreciation
    @Enumerated(EnumType.STRING)
    @Column(name = "depreciation_method", length = 50)
    private DepreciationMethod depreciationMethod;

    @Column(name = "useful_life_years")
    private Integer usefulLifeYears;

    @Column(name = "salvage_value", precision = 19, scale = 2)
    private BigDecimal salvageValue;

    // Assume images are stored internally in DB nor externally thus not using URLs
    @ElementCollection
    @Column(name = "images", nullable = true, columnDefinition = "MEDIUMBLOB")
    private List<byte[]> images;

    /* INFO: using assetAttributeValues to store any new attributes of the asset using EVA (Entity-Value-Attribute) model
        This is a one-to-many relationship with AssetAttributeValue and has a cascade type of ALL and orphanRemoval set to true
        TODO: MBH: This means that if an asset is removed, all its attribute values will be removed as well which needs to be tested
        MBH: this annotation fucked me! find out why?
        @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
     */
    @OneToMany(mappedBy = "asset", fetch = FetchType.LAZY)
    private List<AssetAttributeValue> assetAttributeValues;

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

    public String getAssetDescription() {
        return assetDescription;
    }

    public void setAssetDescription(String itemDescription) {
        this.assetDescription = itemDescription;
    }

    public AssetGroup getAssetGroup() {
        return assetGroup;
    }

    public void setAssetGroup(AssetGroup assetGroup) {
        this.assetGroup = assetGroup;
    }



    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }



    public List<byte[]> getImages() {
        return images;
    }

    public void setImages(List<byte[]> images) {
        this.images = images;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }



    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<AssetAttributeValue> getAssetAttributeValues() {
        return assetAttributeValues;
    }

    public void setAssetAttributeValues(List<AssetAttributeValue> assetAttributeValues) {
        this.assetAttributeValues = assetAttributeValues;
    }

    //toString method
    @Override
    public String toString() {
        return "Asset{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", assetDescription='" + assetDescription + '\'' +
                '}';
    }

    public enum Status {
        ACTIVE, INACTIVE, DISPOSED, UNDER_MAINTENANCE
    }

    public enum DepreciationMethod {
        STRAIGHT_LINE, DECLINING_BALANCE, NONE
    }

    // Getters and setters for new fields

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public DepreciationMethod getDepreciationMethod() {
        return depreciationMethod;
    }

    public void setDepreciationMethod(DepreciationMethod depreciationMethod) {
        this.depreciationMethod = depreciationMethod;
    }

    public Integer getUsefulLifeYears() {
        return usefulLifeYears;
    }

    public void setUsefulLifeYears(Integer usefulLifeYears) {
        this.usefulLifeYears = usefulLifeYears;
    }

    public BigDecimal getSalvageValue() {
        return salvageValue;
    }

    public void setSalvageValue(BigDecimal salvageValue) {
        this.salvageValue = salvageValue;
    }
}
