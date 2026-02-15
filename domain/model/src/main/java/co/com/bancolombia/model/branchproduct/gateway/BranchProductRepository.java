package co.com.bancolombia.model.branchproduct.gateway;

import co.com.bancolombia.model.branchproduct.BranchProduct;
import reactor.core.publisher.Mono;

public interface BranchProductRepository {

    Mono<BranchProduct> save(BranchProduct branchProduct);

    Mono<BranchProduct> findByBranchAndProduct(Long branchId, Long productId);

    Mono<Void> softDelete(Long branchId, Long productId);

    Mono<BranchProduct> findActiveByBranchAndProduct(Long branchId, Long productId);

    Mono<BranchProduct> updateStock(Long branchId, Long productId, Integer stock);
}
