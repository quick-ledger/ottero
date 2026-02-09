package io.quickledger.services.asset.validator.def;

import io.quickledger.entities.asset.AssetAttributeDefinition;
import io.quickledger.services.asset.validator.ValidationResult;
import org.apache.commons.lang3.StringUtils;

public class AttributeDefinitionSaveValidator {
    public ValidationResult validate(AssetAttributeDefinition entity) {
        return default_value_should_match_value_type(entity).
                next(field_value_type_required(entity));
    }


    private ValidationResult default_value_should_match_value_type(AssetAttributeDefinition entity){
        if(entity.getDefaultValue() != null){
            if(StringUtils.isNumeric(entity.getDefaultValue()) && entity.getFieldValueType() == AssetAttributeDefinition.ValueType.NUMBER){
                return ValidationResult.OK;
            }
            if(StringUtils.isAlpha(entity.getDefaultValue()) && entity.getFieldValueType() == AssetAttributeDefinition.ValueType.TEXT){
                return ValidationResult.OK;
            }
            //TODO handle more cases
            return new ValidationResult(false, entity.getName() + " Default value should match the field value type");
        }

        return ValidationResult.OK;
    }

    // Field Value type is required
    private ValidationResult field_value_type_required(AssetAttributeDefinition entity){
            if(entity.getFieldValueType() == null)
                    return new ValidationResult(false, entity.getName() + " Field value type is required");
        else return ValidationResult.OK;
}


}
