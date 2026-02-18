package co.com.bancolombia.model.usecase;

import reactor.core.publisher.Mono;

public abstract class RemoveProductFromBranchPort {
    public abstract Mono<Void> execute(Long branchId, Long productId);
}
