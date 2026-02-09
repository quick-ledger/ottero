package io.quickledger.services.asset.validator.values;

import io.quickledger.entities.asset.AssetAttributeValue;
import io.quickledger.services.asset.validator.SaveValidator;
import io.quickledger.services.asset.validator.ValidationResult;

/**
Mandatory values should be present

 */
public class MandatoryValuesValidator implements SaveValidator<AssetAttributeValue> {
    @Override
    public ValidationResult validate(AssetAttributeValue entity) {

        if (entity.getDefinition().getRequired() &&
                (entity.getValue() == null && entity.getSelectedValueItem()== null && entity.getSelectedUnitItem() == null)) {
            return new ValidationResult(false, entity.getDefinition().getName() + " needs a required value");
        }
        return ValidationResult.OK;
    }
}
