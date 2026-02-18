package co.com.bancolombia.model.usecase;

import co.com.bancolombia.model.franchise.Franchise;
import reactor.core.publisher.Mono;

public abstract class UpdateFranchiseNamePort {
    public abstract Mono<Franchise> execute(Long franchiseId, String newName);
}
