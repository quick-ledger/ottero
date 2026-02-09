package io.quickledger.services.asset.validator.def;

import io.quickledger.entities.asset.AssetAttributeDefinition;
import io.quickledger.entities.asset.AssetAttributeDefinition.ValueType;
import io.quickledger.services.asset.validator.UpdateValidator;
import io.quickledger.services.asset.validator.ValidationResult;

/**
 * Default value should match the field value type

 */
public class TypeCheckUpdateValidator implements UpdateValidator<AssetAttributeDefinition, AssetAttributeDefinition> {
    @Override
    public ValidationResult validate(AssetAttributeDefinition existing, AssetAttributeDefinition newValue, long assetCount) {
        AssetAttributeDefinition.ValueType existingType=  existing.getFieldValueType();
        AssetAttributeDefinition.ValueType newType=  newValue.getFieldValueType();
        //FILE, TEXT, NUMBER, DATE, BOOLEAN, SELECTABLE

        //text -> number
        if(assetCount > 0){
            String generic = "This field type selection is not compatible with the existing asset values";
            if(existingType.equals(ValueType.TEXT) && newType.equals(ValueType.NUMBER)){
                return new ValidationResult(false, "Cannot change field type from text to number when there are assets with this attribute");
            }

            if(existingType.equals(ValueType.TEXT) && newType.equals(ValueType.BOOLEAN)){
                return new ValidationResult(false, generic);
            }

            if(existingType.equals(ValueType.FILE) && !newType.equals(ValueType.FILE)){
                return new ValidationResult(false, generic);
            }


        }


        return ValidationResult.OK;
    }
}
