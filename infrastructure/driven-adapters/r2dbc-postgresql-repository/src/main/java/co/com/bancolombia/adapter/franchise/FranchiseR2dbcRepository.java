package co.com.bancolombia.adapter.franchise;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface FranchiseR2dbcRepository extends ReactiveCrudRepository<FranchiseData, Long> {

    Mono<Boolean> existsByName(String name);
}
