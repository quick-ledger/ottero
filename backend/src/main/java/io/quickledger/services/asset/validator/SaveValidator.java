package io.quickledger.services.asset.validator;

public interface SaveValidator<T> {
    ValidationResult validate(T value);
}
