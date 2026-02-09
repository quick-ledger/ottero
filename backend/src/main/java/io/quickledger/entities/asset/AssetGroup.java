package io.quickledger.entities.asset;

import io.quickledger.entities.BaseEntity;
import io.quickledger.entities.Company;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "asset_groups") // This names the table as "asset_groups" in the database

/**
 * this is asset definition or asset type. used for grouping assets (asset instances)
 */
public class AssetGroup extends BaseEntity {
    public AssetGroup() {
    }

    //Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assetGroupName", length = 500, nullable = false)
    private String assetGroupName;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", referencedColumnName = "id", nullable = false)
    private Company company;

    // Assume images are stored internally in DB nor externally thus not using URLs
    @ElementCollection
    @Column(name = "images", nullable = true, columnDefinition="MEDIUMBLOB")
    private List<byte[]> images;


    // Getters and Setters
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

    //toString method
    @Override
    public String toString() {
        return "AssetGroup{" +
                "id=" + id +
                ", assetGroupName='" + assetGroupName + '\'' +
                ", images=" + images + '\'' +
                ", baseEntity=" + super.toString() + '\'' +
                '}';
    }
}
