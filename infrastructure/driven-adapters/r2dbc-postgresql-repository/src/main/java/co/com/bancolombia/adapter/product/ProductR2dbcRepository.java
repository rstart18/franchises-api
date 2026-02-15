package co.com.bancolombia.adapter.product;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductR2dbcRepository extends ReactiveCrudRepository<ProductData, Long> {

    Mono<ProductData> findByName(String name);

    Mono<Boolean> existsByName(String name);
}
