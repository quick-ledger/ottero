package io.quickledger.services.asset.validator;

import java.util.ArrayList;
import java.util.List;

public class CompositeSaveValidator<T> implements SaveValidator<T> {
    private final List<SaveValidator<T>> validators = new ArrayList<>();

    public CompositeSaveValidator<T> addValidator(SaveValidator<T> validator) {
        validators.add(validator);
        return this;
    }

    @Override
    public ValidationResult validate(T value) {
        for (SaveValidator<T> validator : validators) {
            ValidationResult r = validator.validate(value);
            if (!r.isValid()) return r  ;
        }
        return ValidationResult.OK;
    }
}