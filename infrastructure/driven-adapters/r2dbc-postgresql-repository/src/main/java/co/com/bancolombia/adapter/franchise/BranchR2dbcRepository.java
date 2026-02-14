package co.com.bancolombia.adapter.franchise;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface BranchR2dbcRepository extends ReactiveCrudRepository<BranchData, Long> {

    Flux<BranchData> findAllByFranchiseId(Long franchiseId);
}
