package co.com.bancolombia.model.branchproduct;

public record StockAlert(
        Long productId,
        Long branchId,
        String productName,
        Integer currentStock
) {
    public static StockAlert from(BranchProduct branchProduct) {
        return new StockAlert(
                branchProduct.productId(),
                branchProduct.branchId(),
                branchProduct.productName(),
                branchProduct.stock()
        );
    }
}
