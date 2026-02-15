package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateway.BranchRepository;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.franchise.gateway.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AddBranchToFranchiseUseCase {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;

    public Mono<Branch> execute(Long franchiseId, Branch branch) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.FRANCHISE_NOT_FOUND)))
                .then(Mono.just(branch))
                .filter(b -> b.name() != null && !b.name().isBlank())
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BRANCH_NAME_REQUIRED)))
                .flatMap(b -> branchRepository.save(b, franchiseId));
    }
}
