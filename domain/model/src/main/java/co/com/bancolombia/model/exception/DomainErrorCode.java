package co.com.bancolombia.model.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DomainErrorCode {

    FRANCHISE_NAME_REQUIRED(400, "Franchise name is required"),
    FRANCHISE_NAME_ALREADY_EXISTS(409, "A franchise with that name already exists"),
    FRANCHISE_NOT_FOUND(404, "Franchise not found"),
    BRANCH_NAME_REQUIRED(400, "Branch name is required"),
    BRANCH_NOT_FOUND(404, "Branch not found"),
    PRODUCT_NAME_REQUIRED(400, "Product name is required"),
    PRODUCT_STOCK_INVALID(400, "Product stock must be zero or greater"),
    PRODUCT_NAME_ALREADY_EXISTS(409, "A product with that name already exists"),
    PRODUCT_NOT_FOUND(404, "Product not found");

    private final int statusCode;
    private final String defaultMessage;

    public String getCode() {
        return this.name();
    }
}
