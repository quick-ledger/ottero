package io.quickledger.dto.product;

public class ProductDefinitionRawDto {
    private String Name;
    private Enum attributeType;
    private String defaultValue;
    private boolean isRequired;
    private String unit;
    private String pattern;

    //define attributeType enum
    public enum attributeType {
        STRING,
        NUMBER,
        BOOLEAN,
        DATETIME
    }
}
