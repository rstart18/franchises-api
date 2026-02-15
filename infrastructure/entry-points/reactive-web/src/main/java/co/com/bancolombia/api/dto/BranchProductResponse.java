package co.com.bancolombia.api.dto;

public record BranchProductResponse(
        Long productId,
        Long branchId,
        String productName,
        Integer stock
) {}
