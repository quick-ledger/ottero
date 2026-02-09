package io.quickledger.services.asset.validator;

import java.util.Optional;

public interface UpdateValidator<T,R> {
    ValidationResult validate(T existingEntity, R newEntity, long currentAssetCount);
}
