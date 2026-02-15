package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateway.BranchRepository;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.franchise.gateway.FranchiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddBranchToFranchiseUseCaseTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchRepository branchRepository;

    private AddBranchToFranchiseUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AddBranchToFranchiseUseCase(franchiseRepository, branchRepository);
    }

    @Test
    @DisplayName("Should add branch successfully when franchise exists and branch name is valid")
    void shouldAddBranchSuccessfully() {
        Long franchiseId = 1L;
        Branch branch = new Branch(null, "North Branch");
        Branch savedBranch = new Branch(10L, "North Branch");
        Franchise franchise = Franchise.builder().id(franchiseId).name("Burger Kingdom").build();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));
        when(branchRepository.save(branch, franchiseId)).thenReturn(Mono.just(savedBranch));

        StepVerifier.create(useCase.execute(franchiseId, branch))
                .expectNextMatches(result -> result.id().equals(10L) && result.name().equals("North Branch"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should fail with FRANCHISE_NOT_FOUND when franchise does not exist")
    void shouldFailWhenFranchiseNotFound() {
        Long franchiseId = 99L;
        Branch branch = new Branch(null, "North Branch");

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(franchiseId, branch))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.FRANCHISE_NOT_FOUND)
                .verify();

        verify(branchRepository, never()).save(any(), any());
    }

    @Test
    @DisplayName("Should fail with BRANCH_NAME_REQUIRED when branch name is null")
    void shouldFailWhenBranchNameIsNull() {
        Long franchiseId = 1L;
        Branch branch = new Branch(null, null);
        Franchise franchise = Franchise.builder().id(franchiseId).name("Burger Kingdom").build();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));

        StepVerifier.create(useCase.execute(franchiseId, branch))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.BRANCH_NAME_REQUIRED)
                .verify();

        verify(branchRepository, never()).save(any(), any());
    }

    @Test
    @DisplayName("Should fail with BRANCH_NAME_REQUIRED when branch name is blank")
    void shouldFailWhenBranchNameIsBlank() {
        Long franchiseId = 1L;
        Branch branch = new Branch(null, "   ");
        Franchise franchise = Franchise.builder().id(franchiseId).name("Burger Kingdom").build();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));

        StepVerifier.create(useCase.execute(franchiseId, branch))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.BRANCH_NAME_REQUIRED)
                .verify();

        verify(branchRepository, never()).save(any(), eq(franchiseId));
    }
}
