package co.com.bancolombia.model.usecase;

import co.com.bancolombia.model.branch.Branch;
import reactor.core.publisher.Mono;

public abstract class AddBranchToFranchisePort {
    public abstract Mono<Branch> execute(Long franchiseId, Branch branch);
}
