package io.quickledger.entities.asset;

import io.quickledger.entities.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "asset_attribute_definitions")
/**
 * Field attributes for an asset
 */
public class AssetAttributeDefinition extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; //field name. e.g. weight, SKU, etc.

    private Boolean required; //attribute field is required or not
    private Boolean disabled;
    private Boolean visible;
    @Column(name = "field_order")
    private Integer order;

    private String defaultValue;

    @Enumerated(EnumType.STRING)
    private ValueType fieldValueType;

    public enum ValueType{
        FILE, TEXT, NUMBER, DATE, BOOLEAN, SELECTABLE
    }
    @ManyToOne(cascade = CascadeType.REFRESH)
    private Selectable fieldValueUnit; //what is the unit of the value e.g. weight is 56 kg

    @ManyToOne(cascade = CascadeType.REFRESH)
    private Selectable fieldSelectableValue; //value is limited to a selectable. e.g. color is red, blue, green only.


    private String regex;
    private String format; // e.g. date format
    private String validation; // e.g. email, phone, etc. validation
    @Column(name = "constraint_value")
    private String constraint; // e.g. min value , max value, etc.

    @ManyToOne(fetch = FetchType.EAGER)
    AssetGroup assetGroup;


    // getters and setters


    public Selectable getFieldSelectableValue() {
        return fieldSelectableValue;
    }

    public void setFieldSelectableValue(Selectable fieldSelectableValue) {
        this.fieldSelectableValue = fieldSelectableValue;
    }

    public AssetGroup getAssetGroup() {
        return assetGroup;
    }

    public void setAssetGroup(AssetGroup assetGroup) {
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

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ValueType getFieldValueType() {
        return fieldValueType;
    }

    public void setFieldValueType(ValueType fieldValueType) {
        this.fieldValueType = fieldValueType;
    }

    public Selectable getFieldValueUnit() {
        return fieldValueUnit;
    }

    public void setFieldValueUnit(Selectable fieldValueUnit) {
        this.fieldValueUnit = fieldValueUnit;
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
}