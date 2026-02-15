package co.com.bancolombia.adapter.branchproduct;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BranchProductR2dbcRepository extends ReactiveCrudRepository<BranchProductData, Long> {

    @Query("SELECT * FROM branch_products WHERE branch_id = :branchId AND product_id = :productId")
    Mono<BranchProductData> findByBranchAndProduct(Long branchId, Long productId);
}
