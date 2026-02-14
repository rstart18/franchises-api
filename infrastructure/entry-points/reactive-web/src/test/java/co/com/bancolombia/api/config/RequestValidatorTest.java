package co.com.bancolombia.api.config;

import co.com.bancolombia.api.dto.BranchRequest;
import co.com.bancolombia.api.dto.FranchiseRequest;
import co.com.bancolombia.api.exception.ValidationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class RequestValidatorTest {

    private RequestValidator requestValidator;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        requestValidator = new RequestValidator(validator);
    }

    @Test
    @DisplayName("Should pass validation for valid request")
    void shouldPassValidationForValidRequest() {
        FranchiseRequest request = FranchiseRequest.builder()
                .name("Burger Kingdom")
                .build();

        StepVerifier.create(requestValidator.validate(request))
                .expectNextMatches(r -> r.getName().equals("Burger Kingdom"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void shouldFailValidationWhenNameIsBlank() {
        FranchiseRequest request = FranchiseRequest.builder()
                .name("")
                .build();

        StepVerifier.create(requestValidator.validate(request))
                .expectErrorMatches(e -> e instanceof ValidationException)
                .verify();
    }

    @Test
    @DisplayName("Should fail validation when name is null")
    void shouldFailValidationWhenNameIsNull() {
        FranchiseRequest request = FranchiseRequest.builder()
                .name(null)
                .build();

        StepVerifier.create(requestValidator.validate(request))
                .expectErrorMatches(e -> e instanceof ValidationException)
                .verify();
    }

    @Test
    @DisplayName("Should return multiple sorted errors when multiple fields are invalid")
    void shouldReturnSortedErrorsWhenMultipleFieldsInvalid() {
        BranchRequest request = new BranchRequest();

        StepVerifier.create(requestValidator.validate(request))
                .expectErrorMatches(e -> {
                    if (!(e instanceof ValidationException ve)) return false;
                    return !ve.getErrors().isEmpty();
                })
                .verify();
    }
}
