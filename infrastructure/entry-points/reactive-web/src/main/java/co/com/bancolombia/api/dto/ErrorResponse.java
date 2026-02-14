package co.com.bancolombia.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int status;
    private final String code;
    private final String message;
    private final List<FieldError> errors;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    public static ErrorResponse of(int status, String message, LocalDateTime timestamp) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .timestamp(timestamp)
                .build();
    }

    public static ErrorResponse ofBusiness(int status, String code, String message, LocalDateTime timestamp) {
        return ErrorResponse.builder()
                .status(status)
                .code(code)
                .message(message)
                .timestamp(timestamp)
                .build();
    }

    public static ErrorResponse ofValidation(int status, List<FieldError> errors, LocalDateTime timestamp) {
        return ErrorResponse.builder()
                .status(status)
                .message("Validation failed")
                .errors(errors)
                .timestamp(timestamp)
                .build();
    }

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String message;

        public static FieldError of(String field, String message) {
            return FieldError.builder().field(field).message(message).build();
        }
    }
}
