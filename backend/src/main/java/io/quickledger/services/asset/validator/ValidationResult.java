package io.quickledger.services.asset.validator;

import java.util.function.Function;

public class ValidationResult {
    public static final ValidationResult OK = new ValidationResult(true, null);
    private boolean valid;
    private String message;



    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public ValidationResult next(ValidationResult nextValidation) {
        return this.valid ? nextValidation : this;
    }

    public ValidationResult ifValid(Function<ValidationResult, ValidationResult> nextValidation) {
        return this.valid ? nextValidation.apply(this) : this;
    }
    public String getMessage() {
        return message;
    }
}
