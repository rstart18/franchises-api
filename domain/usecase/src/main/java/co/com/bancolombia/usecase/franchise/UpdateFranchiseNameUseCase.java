package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.franchise.gateway.FranchiseRepository;
import co.com.bancolombia.model.usecase.UpdateFranchiseNamePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateFranchiseNameUseCase extends UpdateFranchiseNamePort {

    private final FranchiseRepository franchiseRepository;

    @Override
    public Mono<Franchise> execute(Long franchiseId, String newName) {
        return Mono.justOrEmpty(newName)
                .filter(name -> !name.isBlank())
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.FRANCHISE_NAME_REQUIRED)))
                .flatMap(name -> franchiseRepository.findById(franchiseId))
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.FRANCHISE_NOT_FOUND)))
                .flatMap(franchise -> franchiseRepository.existsByName(newName)
                        .filter(exists -> !exists)
                        .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.FRANCHISE_NAME_ALREADY_EXISTS)))
                        .then(Mono.defer(() -> franchiseRepository.updateName(franchiseId, newName)))
                );
    }
}
