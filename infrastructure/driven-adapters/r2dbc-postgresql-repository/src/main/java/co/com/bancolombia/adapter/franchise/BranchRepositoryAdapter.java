package co.com.bancolombia.adapter.franchise;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateway.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
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
}
