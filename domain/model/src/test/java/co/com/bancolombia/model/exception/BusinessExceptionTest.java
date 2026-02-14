package co.com.bancolombia.model.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BusinessExceptionTest {

    @Test
    @DisplayName("Should create BusinessException with error code")
    void shouldCreateWithErrorCode() {
        BusinessException ex = new BusinessException(DomainErrorCode.FRANCHISE_NAME_REQUIRED);
        assertEquals("FRANCHISE_NAME_REQUIRED", ex.getErrorCode().getCode());
        assertEquals(DomainErrorCode.FRANCHISE_NAME_REQUIRED.getDefaultMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("All DomainErrorCodes should have non-null codes and messages")
    void allErrorCodesShouldHaveNonNullCodesAndMessages() {
        for (DomainErrorCode code : DomainErrorCode.values()) {
            assertNotNull(code.getCode());
            assertEquals(code.name(), code.getCode());
            assertNotNull(code.getDefaultMessage());
            assertNotNull(code.getStatusCode());
        }
    }
}
