package co.com.bancolombia.model.exception;

import lombok.Getter;

@Getter
public class TechnicalException extends RuntimeException {

    private static final long serialVersionUID = 1002L;

    private final String code;

    public TechnicalException(DomainErrorCode errorCode, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.code = errorCode.getCode();
    }
}
