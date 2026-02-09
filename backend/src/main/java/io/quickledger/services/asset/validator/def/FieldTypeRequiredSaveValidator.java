package io.quickledger.services.asset.validator.def;

import io.quickledger.entities.asset.AssetAttributeDefinition;
import io.quickledger.services.asset.validator.SaveValidator;
import io.quickledger.services.asset.validator.ValidationResult;
import org.apache.commons.lang3.StringUtils;

/**
 * Default value should match the field value type

 */
public class FieldTypeRequiredSaveValidator implements SaveValidator<AssetAttributeDefinition> {
    @Override
    public ValidationResult validate(AssetAttributeDefinition entity) {
        if(entity.getFieldValueType() == null)
            return new ValidationResult(false, entity.getName() + " Field value type is required");
        else return ValidationResult.OK;
    }
}
