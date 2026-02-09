package io.quickledger.services.asset.validator.def;

import io.quickledger.entities.asset.AssetAttributeDefinition;
import io.quickledger.services.asset.validator.UpdateValidator;
import io.quickledger.services.asset.validator.ValidationResult;

/**
 * Default value should match the field value type

 */
public class NotRequiredToRequiredUpdateValidator implements UpdateValidator<AssetAttributeDefinition, AssetAttributeDefinition> {
    @Override
    public ValidationResult validate(AssetAttributeDefinition existing, AssetAttributeDefinition newValue, long assetCount) {

        if (existing.getRequired() != newValue.getRequired()) {
            if (newValue.getRequired() && newValue.getDefaultValue() == null) {
                return new ValidationResult(false, "Default value is required for required field");
            }


        }
        return ValidationResult.OK;
    }
}
