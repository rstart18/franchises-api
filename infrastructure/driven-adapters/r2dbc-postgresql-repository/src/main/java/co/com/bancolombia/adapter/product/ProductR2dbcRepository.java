package co.com.bancolombia.adapter.product;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductR2dbcRepository extends ReactiveCrudRepository<ProductData, Long> {
}
