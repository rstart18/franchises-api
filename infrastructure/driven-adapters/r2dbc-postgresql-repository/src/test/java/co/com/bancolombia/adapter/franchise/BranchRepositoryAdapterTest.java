package co.com.bancolombia.adapter.franchise;

import co.com.bancolombia.model.branch.Branch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchRepositoryAdapterTest {

    @Mock
    private BranchR2dbcRepository branchR2dbcRepository;

    @Mock
    private FranchiseDataMapper mapper;

    private BranchRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BranchRepositoryAdapter(branchR2dbcRepository, mapper);
    }

    @Test
    @DisplayName("Should save branch and return domain object with generated id")
    void shouldSaveBranchAndReturnDomainObject() {
        Branch branch = new Branch(null, "North Branch");
        Long franchiseId = 1L;
        BranchData branchData = BranchData.builder().name("North Branch").franchiseId(franchiseId).build();
        BranchData savedData = BranchData.builder().id(10L).name("North Branch").franchiseId(franchiseId).build();
        Branch domainResult = new Branch(10L, "North Branch");

        when(mapper.branchToData(branch, franchiseId)).thenReturn(branchData);
        when(branchR2dbcRepository.save(branchData)).thenReturn(Mono.just(savedData));
        when(mapper.branchToDomain(savedData)).thenReturn(domainResult);

        StepVerifier.create(adapter.save(branch, franchiseId))
                .expectNextMatches(result -> result.id().equals(10L) && result.name().equals("North Branch"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find branch by id successfully")
    void shouldFindBranchByIdSuccessfully() {
        Long branchId = 10L;
        BranchData branchData = BranchData.builder().id(branchId).name("North Branch").franchiseId(1L).build();
        Branch expectedBranch = new Branch(branchId, "North Branch");

        when(branchR2dbcRepository.findById(branchId)).thenReturn(Mono.just(branchData));
        when(mapper.branchToDomain(branchData)).thenReturn(expectedBranch);

        StepVerifier.create(adapter.findById(branchId))
                .expectNextMatches(result -> result.id().equals(branchId) && result.name().equals("North Branch"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when branch not found")
    void shouldReturnEmptyWhenBranchNotFound() {
        Long branchId = 999L;

        when(branchR2dbcRepository.findById(branchId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(branchId))
                .verifyComplete();
    }
}
