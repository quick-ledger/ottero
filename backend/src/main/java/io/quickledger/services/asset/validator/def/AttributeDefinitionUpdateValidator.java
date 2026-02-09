package io.quickledger.services.asset.validator.def;

import io.quickledger.entities.asset.AssetAttributeDefinition;
import io.quickledger.services.asset.validator.ValidationResult;
import org.apache.commons.lang3.StringUtils;

public class AttributeDefinitionUpdateValidator {
    public ValidationResult validate(AssetAttributeDefinition existing, AssetAttributeDefinition newValue, long assetCount) {
        return non_required_to_required_needs_default_value(existing,newValue);
    }


    //non-required to required needs default value
    public ValidationResult non_required_to_required_needs_default_value(AssetAttributeDefinition existing, AssetAttributeDefinition newValue) {
        if (newValue.getRequired() && newValue.getDefaultValue() == null) {
            return new ValidationResult(false, newValue.getName() + " Default value is required for a required field");
        }
        return ValidationResult.OK;
    }
}
