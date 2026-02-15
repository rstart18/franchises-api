package co.com.bancolombia.model.branch.gateway;

import co.com.bancolombia.model.branch.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchRepository {

    Mono<Branch> save(Branch branch, Long franchiseId);

    Mono<Branch> findById(Long id);

    Flux<Branch> findAllByFranchiseId(Long franchiseId);

    Mono<Boolean> existsByName(String name);

    Mono<Branch> updateName(Long id, String newName);
}
