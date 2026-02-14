package co.com.bancolombia.api.config;

import co.com.bancolombia.api.dto.ErrorResponse.FieldError;
import co.com.bancolombia.api.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    public <T> Mono<T> validate(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return Mono.just(request);
        }
        List<FieldError> errors = violations.stream()
                .map(v -> FieldError.of(v.getPropertyPath().toString(), v.getMessage()))
                .sorted((a, b) -> a.getField().compareTo(b.getField()))
                .toList();
        return Mono.error(new ValidationException(errors));
    }
}
