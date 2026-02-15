package co.com.bancolombia.model.branchproduct;

public record TopStockProduct(
        Long productId,
        String productName,
        Integer stock,
        Long branchId,
        String branchName
) {}
