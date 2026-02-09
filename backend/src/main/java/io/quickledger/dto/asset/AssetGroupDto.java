package io.quickledger.dto.asset;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class AssetGroupDto {

    private Long id;
    private String assetGroupName;
    //TODO: in future need to handle image outside dto as it's binary object and not a good practice to keep it in dto
    //@JsonIgnore
    //private List<byte[]> images;
    private Long companyId;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssetGroupName() {
        return assetGroupName;
    }

    public void setAssetGroupName(String assetGroupName) {
        this.assetGroupName = assetGroupName;
    }

//    public List<byte[]> getImages() {
//        return images;
//    }
//
//    public void setImages(List<byte[]> images) {
//        this.images = images;
//    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}