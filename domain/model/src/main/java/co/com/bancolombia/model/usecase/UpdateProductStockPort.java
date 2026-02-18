package co.com.bancolombia.model.usecase;

import co.com.bancolombia.model.branchproduct.BranchProduct;
import reactor.core.publisher.Mono;

public abstract class UpdateProductStockPort {
    public abstract Mono<BranchProduct> execute(Long branchId, Long productId, Integer stock);
}
