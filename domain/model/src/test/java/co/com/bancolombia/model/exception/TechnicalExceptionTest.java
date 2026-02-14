package co.com.bancolombia.model.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class TechnicalExceptionTest {

    @Test
    @DisplayName("Should create TechnicalException with error code and cause")
    void shouldCreateWithErrorCodeAndCause() {
        Throwable cause = new RuntimeException("DB connection failed");
        TechnicalException ex = new TechnicalException(DomainErrorCode.FRANCHISE_NOT_FOUND, cause);
        assertEquals("FRANCHISE_NOT_FOUND", ex.getCode());
        assertEquals(DomainErrorCode.FRANCHISE_NOT_FOUND.getDefaultMessage(), ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
