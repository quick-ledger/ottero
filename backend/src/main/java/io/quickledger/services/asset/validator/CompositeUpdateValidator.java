package io.quickledger.services.asset.validator;

import org.hibernate.sql.Update;

import java.util.ArrayList;
import java.util.List;

public class CompositeUpdateValidator<T,R> implements UpdateValidator<T,R> {
    private final List<UpdateValidator<T,R>> validators = new ArrayList<>();

    public CompositeUpdateValidator<T,R> addValidator(UpdateValidator<T,R> validator) {
        validators.add(validator);
        return this;
    }

    @Override
    public ValidationResult validate(T existing, R newValue, long usageCount) {
        for (UpdateValidator<T,R> validator : validators) {
            ValidationResult r = validator.validate(existing, newValue, usageCount);
            if (!r.isValid()) return r  ;
        }
        return ValidationResult.OK;
    }

}