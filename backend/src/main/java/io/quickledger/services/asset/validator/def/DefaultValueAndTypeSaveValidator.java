package io.quickledger.services.asset.validator.def;

import io.quickledger.entities.asset.AssetAttributeDefinition;
import io.quickledger.services.asset.validator.SaveValidator;
import io.quickledger.services.asset.validator.ValidationResult;
import org.apache.commons.lang3.StringUtils;

/**
 * Default value should match the field value type

 */
public class DefaultValueAndTypeSaveValidator implements SaveValidator<AssetAttributeDefinition> {
    @Override
    public ValidationResult validate(AssetAttributeDefinition entity) {
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
}
