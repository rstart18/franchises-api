package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.franchise.gateway.FranchiseRepository;
import co.com.bancolombia.model.usecase.CreateFranchisePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreateFranchiseUseCase extends CreateFranchisePort {

    private final FranchiseRepository franchiseRepository;

    @Override
    public Mono<Franchise> execute(Franchise franchise) {
        return Mono.just(franchise)
                .filter(this::hasValidName)
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.FRANCHISE_NAME_REQUIRED)))
                .filterWhen(f -> franchiseRepository.existsByName(f.getName()).map(exists -> !exists))
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.FRANCHISE_NAME_ALREADY_EXISTS)))
                .flatMap(franchiseRepository::save);
    }

    private boolean hasValidName(Franchise franchise) {
        return franchise.getName() != null && !franchise.getName().isBlank();
    }
}
