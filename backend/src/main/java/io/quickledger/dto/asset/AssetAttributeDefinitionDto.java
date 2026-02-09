package io.quickledger.dto.asset;

import io.quickledger.entities.asset.AssetAttributeDefinition;
import io.quickledger.entities.asset.AssetGroup;
import io.quickledger.entities.asset.Selectable;
import jakarta.persistence.*;

public class AssetAttributeDefinitionDto {

    private Long id;
    private String name;

    private String defaultValue;

    private String regex;
    private String format; // e.g. date format
    private String validation; // e.g. email, phone, etc. validation
    private String constraint; // e.g. min value , max value, etc.

    AssetGroupDto assetGroup;


//    private Boolean required; //attribute field is required or not
//    private Boolean disabled;
//    private Boolean visible;
//    @Column(name = "field_order")
//    private Integer order;

//
//    @Enumerated(EnumType.STRING)
//    private AssetAttributeDefinition.ValueType fieldValueType;
//
//    public enum ValueType{
//        FILE, TEXT, NUMBER, DATE, BOOLEAN, SELECTABLE
//    private Selectable fieldValueUnit;



    // getters and setters


    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getConstraint() {
        return constraint;
    }

    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    public AssetGroupDto getAssetGroup() {
        return assetGroup;
    }

    public void setAssetGroup(AssetGroupDto assetGroup) {
        this.assetGroup = assetGroup;
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
}