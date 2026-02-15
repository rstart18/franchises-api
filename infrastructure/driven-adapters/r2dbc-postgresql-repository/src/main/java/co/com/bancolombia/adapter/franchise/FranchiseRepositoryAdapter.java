package co.com.bancolombia.adapter.franchise;

import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.franchise.gateway.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class FranchiseRepositoryAdapter implements FranchiseRepository {

    private final FranchiseR2dbcRepository franchiseR2dbcRepository;
    private final FranchiseDataMapper mapper;

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return franchiseR2dbcRepository.save(mapper.toData(franchise))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return franchiseR2dbcRepository.existsByName(name);
    }

    @Override
    public Mono<Franchise> findById(Long id) {
        return franchiseR2dbcRepository.findById(id)
                .map(mapper::toDomain);
    }
}
