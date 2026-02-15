package co.com.bancolombia.api.dto;

public record TopStockProductResponse(
        Long productId,
        String productName,
        Integer stock,
        Long branchId,
        String branchName
) {}
