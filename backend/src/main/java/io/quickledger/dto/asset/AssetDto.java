package io.quickledger.dto.asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AssetDto {

    private Long id;
    private Long companyId;
    private String name;
    private String description;
    private Long assetGroupId;
    private List<AssetAttributeValueDto> valueDTOs;

    // Asset identification
    private String code;
    private String serialNumber;
    private String location;
    private Integer quantity;

    // Financial tracking
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private BigDecimal currentValue;

    // Depreciation
    private String depreciationMethod;
    private Integer usefulLifeYears;
    private BigDecimal salvageValue;

    // Status
    private String status;


    public Long getAssetGroupId() {
        return assetGroupId;
    }

    public void setAssetGroupId(Long assetGroupId) {
        this.assetGroupId = assetGroupId;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AssetAttributeValueDto> getValueDTOs() {
        return valueDTOs;
    }

    public void setValueDTOs(List<AssetAttributeValueDto> valueDTOs) {
        this.valueDTOs = valueDTOs;
    }

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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

    public String getDepreciationMethod() {
        return depreciationMethod;
    }

    public void setDepreciationMethod(String depreciationMethod) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}