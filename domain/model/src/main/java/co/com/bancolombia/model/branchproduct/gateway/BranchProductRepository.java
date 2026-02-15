package co.com.bancolombia.model.branchproduct.gateway;

import co.com.bancolombia.model.branchproduct.BranchProduct;
import reactor.core.publisher.Mono;

public interface BranchProductRepository {

    Mono<BranchProduct> save(BranchProduct branchProduct);

    Mono<BranchProduct> findByBranchAndProduct(Long branchId, Long productId);
}
