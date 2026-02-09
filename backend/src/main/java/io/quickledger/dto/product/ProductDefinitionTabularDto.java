package io.quickledger.dto.product;

import io.quickledger.dto.BaseEntityDto;

import java.util.List;

/**
This is how the json schema will be shown in the UI
 */
public class ProductDefinitionTabularDto extends BaseEntityDto {

    private Long id;
    private String name="";
    private String productDescription="";
    private List<TableRow> rows;

    @Override
    public String toString() {
        return "ProductDefinitionTabularDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", productDescription='" + productDescription + '\'' +
                ", rows=" + rows +
                '}';
    }

    public static class TableRow {
        private String name="";
        private String description="";
        private String required="";
        private String valueType="";
        private String defaultValue="";
        private String unit="";
        private String pattern="";

        @Override
        public String toString() {
            return "TableRow{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", required='" + required + '\'' +
                    ", valueType='" + valueType + '\'' +
                    ", defaultValue='" + defaultValue + '\'' +
                    ", unit='" + unit + '\'' +
                    '}';
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

        public String getRequired() {
            return required;
        }

        public void setRequired(String required) {
            this.required = required;
        }

        public String getValueType() {
            return valueType;
        }

        public void setValueType(String valueType) {
            this.valueType = valueType;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
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

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public void setRows(List<TableRow> rows) {
        this.rows = rows;
    }
}
