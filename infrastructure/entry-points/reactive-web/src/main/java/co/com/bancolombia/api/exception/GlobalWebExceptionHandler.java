package co.com.bancolombia.api.exception;

import co.com.bancolombia.api.dto.ErrorResponse;
import co.com.bancolombia.model.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@Order(-2)
@Slf4j
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return Mono.error(ex);
        }
        log.error("[GlobalExceptionHandler] {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return buildErrorResponse(exchange, ex);
    }

    private Mono<Void> buildErrorResponse(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof ValidationException ve) {
            ErrorResponse body = ErrorResponse.ofValidation(
                    HttpStatus.BAD_REQUEST.value(),
                    ve.getErrors(),
                    LocalDateTime.now()
            );
            return writeResponse(exchange, HttpStatus.BAD_REQUEST, body);
        }

        if (ex instanceof BusinessException be) {
            HttpStatus status = HttpStatus.valueOf(be.getErrorCode().getStatusCode());
            ErrorResponse body = ErrorResponse.ofBusiness(
                    status.value(),
                    be.getErrorCode().getCode(),
                    be.getMessage(),
                    LocalDateTime.now()
            );
            return writeResponse(exchange, status, body);
        }

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                LocalDateTime.now()
        );
        return writeResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, body);
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, HttpStatus status, ErrorResponse body) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(toJson(body).getBytes(StandardCharsets.UTF_8)))
        );
    }

    private String toJson(ErrorResponse error) {
        try {
            return OBJECT_MAPPER.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            return "{\"status\":500,\"message\":\"Internal server error\"}";
        }
    }
}
