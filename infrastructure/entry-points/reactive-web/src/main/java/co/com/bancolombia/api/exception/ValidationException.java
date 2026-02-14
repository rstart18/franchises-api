package co.com.bancolombia.api.exception;

import co.com.bancolombia.api.dto.ErrorResponse.FieldError;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends RuntimeException {

    private final List<FieldError> errors;

    public ValidationException(List<FieldError> errors) {
        super("Validation failed");
        this.errors = errors;
    }
}
