package io.quickledger.entities.asset;

import jakarta.persistence.*;

@Entity
@Table(name = "asset_attribute_values")

/**
 * relates to an asset. all the field values for an asset.
 */
public class AssetAttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)// this can be null if the field is a selectable type
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    private AssetAttributeDefinition definition;

    @ManyToOne
    //@JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne
    private SelectableItem selectedValueItem;

    @ManyToOne
    private SelectableItem selectedUnitItem;


    //------------------------------------------- getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AssetAttributeDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(AssetAttributeDefinition definition) {
        this.definition = definition;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public SelectableItem getSelectedValueItem() {
        return selectedValueItem;
    }

    public void setSelectedValueItem(SelectableItem selectedValueItem) {
        this.selectedValueItem = selectedValueItem;
    }

    public SelectableItem getSelectedUnitItem() {
        return selectedUnitItem;
    }

    public void setSelectedUnitItem(SelectableItem selectedUnitItem) {
        this.selectedUnitItem = selectedUnitItem;
    }
}