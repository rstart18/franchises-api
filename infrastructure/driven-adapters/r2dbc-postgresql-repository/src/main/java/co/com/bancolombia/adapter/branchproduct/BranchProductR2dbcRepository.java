package co.com.bancolombia.adapter.branchproduct;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BranchProductR2dbcRepository extends ReactiveCrudRepository<BranchProductData, Long> {

    @Query("SELECT * FROM branch_products WHERE branch_id = :branchId AND product_id = :productId")
    Mono<BranchProductData> findByBranchAndProduct(Long branchId, Long productId);

    @Query("SELECT * FROM branch_products WHERE branch_id = :branchId AND product_id = :productId AND deleted_at IS NULL")
    Mono<BranchProductData> findActiveByBranchAndProduct(Long branchId, Long productId);

    @Modifying
    @Query("UPDATE branch_products SET deleted_at = NOW() WHERE branch_id = :branchId AND product_id = :productId AND deleted_at IS NULL")
    Mono<Void> softDelete(Long branchId, Long productId);

}
