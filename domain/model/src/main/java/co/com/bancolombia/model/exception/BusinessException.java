package co.com.bancolombia.model.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1001L;

    private final DomainErrorCode errorCode;

    public BusinessException(DomainErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }
}
