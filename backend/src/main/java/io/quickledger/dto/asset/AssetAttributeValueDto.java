package io.quickledger.dto.asset;


public class AssetAttributeValueDto {

    private Long id;
    private String value;
    private AssetDto assetDto;
    private AssetAttributeDefinitionDto definitionDto;
    private SelectableItemDto selectableValueItemDto;
    private SelectableItemDto unitItemDto;


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

    public AssetDto getAssetDto() {
        return assetDto;
    }

    public void setAssetDto(AssetDto assetDto) {
        this.assetDto = assetDto;
    }

    public AssetAttributeDefinitionDto getDefinitionDto() {
        return definitionDto;
    }

    public void setDefinitionDto(AssetAttributeDefinitionDto definitionDto) {
        this.definitionDto = definitionDto;
    }

    public SelectableItemDto getSelectableValueItemDto() {
        return selectableValueItemDto;
    }

    public void setSelectableValueItemDto(SelectableItemDto selectableValueItemDto) {
        this.selectableValueItemDto = selectableValueItemDto;
    }

    public SelectableItemDto getUnitItemDto() {
        return unitItemDto;
    }

    public void setUnitItemDto(SelectableItemDto unitItemDto) {
        this.unitItemDto = unitItemDto;
    }
}