package co.com.bancolombia.api.exception;

import co.com.bancolombia.api.dto.ErrorResponse.FieldError;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import java.util.List;

class GlobalWebExceptionHandlerTest {

    private GlobalWebExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalWebExceptionHandler();
    }

    @Test
    @DisplayName("Should return 404 for FRANCHISE_NOT_FOUND")
    void shouldReturn404ForFranchiseNotFound() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/franchises/999").build());

        StepVerifier.create(handler.handle(exchange, new BusinessException(DomainErrorCode.FRANCHISE_NOT_FOUND)))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.NOT_FOUND;
    }

    @Test
    @DisplayName("Should return 409 for FRANCHISE_NAME_ALREADY_EXISTS")
    void shouldReturn409ForFranchiseNameAlreadyExists() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/franchises").build());

        StepVerifier.create(handler.handle(exchange, new BusinessException(DomainErrorCode.FRANCHISE_NAME_ALREADY_EXISTS)))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.CONFLICT;
    }

    @Test
    @DisplayName("Should return 400 for FRANCHISE_NAME_REQUIRED")
    void shouldReturn400ForFranchiseNameRequired() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/franchises").build());

        StepVerifier.create(handler.handle(exchange, new BusinessException(DomainErrorCode.FRANCHISE_NAME_REQUIRED)))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.BAD_REQUEST;
    }

    @Test
    @DisplayName("Should return 400 with errors array for ValidationException")
    void shouldReturn400WithErrorsArrayForValidationException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/franchises").build());

        List<FieldError> errors = List.of(
                FieldError.of("name", "Franchise name is required"),
                FieldError.of("lastName", "Last name is required")
        );

        StepVerifier.create(handler.handle(exchange, new ValidationException(errors)))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.BAD_REQUEST;
    }

    @Test
    @DisplayName("Should return 500 for unexpected exceptions")
    void shouldReturn500ForUnexpectedException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/franchises").build());

        StepVerifier.create(handler.handle(exchange, new RuntimeException("Unexpected error")))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Test
    @DisplayName("Should propagate ResponseStatusException without handling it")
    void shouldPropagateResponseStatusException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/not-found").build());

        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "No handler found");

        StepVerifier.create(handler.handle(exchange, ex))
                .expectError(ResponseStatusException.class)
                .verify();
    }
}
