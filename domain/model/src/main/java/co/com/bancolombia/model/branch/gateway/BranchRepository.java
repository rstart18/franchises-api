package co.com.bancolombia.model.branch.gateway;

import co.com.bancolombia.model.branch.Branch;
import reactor.core.publisher.Mono;

public interface BranchRepository {

    Mono<Branch> save(Branch branch, Long franchiseId);

    Mono<Branch> findById(Long id);
}
