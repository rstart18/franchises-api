package co.com.bancolombia.model.usecase;

import co.com.bancolombia.model.branch.Branch;
import reactor.core.publisher.Mono;

public abstract class UpdateBranchNamePort {
    public abstract Mono<Branch> execute(Long branchId, String newName);
}
