package io.quickledger.dto.asset;

import java.util.List;

public class AssetDto {

    private Long id;
    private Long companyId;
    private String name;
    private String description;
    private Long assetGroupId;
    private List<AssetAttributeValueDto> valueDTOs;


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
}