package co.com.bancolombia.model.branchproduct;

import co.com.bancolombia.model.product.Product;

public record BranchProduct(
        Long productId,
        Long branchId,
        String productName,
        Integer stock
) {
    /**
     * Creates a BranchProduct from a Product, associating it with a branch and initial stock.
     *
     * @param product the product to associate with the branch
     * @param branchId the branch ID where the product will be added
     * @param stock the initial stock quantity
     * @return a new BranchProduct instance
     */
    public static BranchProduct from(Product product, Long branchId, Integer stock) {
        return new BranchProduct(
                product.id(),
                branchId,
                product.name(),
                stock
        );
    }
}
