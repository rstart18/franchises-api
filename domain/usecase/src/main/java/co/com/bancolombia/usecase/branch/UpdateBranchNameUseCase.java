package co.com.bancolombia.usecase.branch;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateway.BranchRepository;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateBranchNameUseCase {

    private final BranchRepository branchRepository;

    public Mono<Branch> execute(Long branchId, String newName) {
        return Mono.justOrEmpty(newName)
                .filter(name -> !name.isBlank())
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BRANCH_NAME_REQUIRED)))
                .flatMap(name -> branchRepository.findById(branchId))
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BRANCH_NOT_FOUND)))
                .flatMap(branch -> branchRepository.existsByName(newName)
                        .filter(exists -> !exists)
                        .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BRANCH_NAME_ALREADY_EXISTS)))
                        .then(Mono.defer(() -> branchRepository.updateName(branchId, newName)))
                );
    }
}
