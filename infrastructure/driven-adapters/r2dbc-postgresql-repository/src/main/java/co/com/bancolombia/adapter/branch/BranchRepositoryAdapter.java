package co.com.bancolombia.adapter.branch;

import co.com.bancolombia.adapter.franchise.FranchiseDataMapper;
import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateway.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class BranchRepositoryAdapter implements BranchRepository {

    private final BranchR2dbcRepository branchR2dbcRepository;
    private final FranchiseDataMapper mapper;

    @Override
    public Mono<Branch> save(Branch branch, Long franchiseId) {
        return branchR2dbcRepository.save(mapper.branchToData(branch, franchiseId))
                .map(mapper::branchToDomain);
    }

    @Override
    public Mono<Branch> findById(Long id) {
        return branchR2dbcRepository.findById(id)
                .map(mapper::branchToDomain);
    }

    @Override
    public Flux<Branch> findAllByFranchiseId(Long franchiseId) {
        return branchR2dbcRepository.findAllByFranchiseId(franchiseId)
                .map(mapper::branchToDomain);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return branchR2dbcRepository.existsByName(name);
    }

    @Override
    public Mono<Branch> updateName(Long id, String newName) {
        return branchR2dbcRepository.findById(id)
                .flatMap(data -> {
                    data.setName(newName);
                    return branchR2dbcRepository.save(data);
                })
                .map(mapper::branchToDomain);
    }
}
