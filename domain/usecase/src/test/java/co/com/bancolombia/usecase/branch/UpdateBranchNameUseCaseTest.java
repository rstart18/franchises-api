package co.com.bancolombia.usecase.branch;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateway.BranchRepository;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateBranchNameUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    private UpdateBranchNameUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateBranchNameUseCase(branchRepository);
    }

    @Test
    @DisplayName("Should update branch name successfully when name is valid, branch exists and name is unique")
    void shouldUpdateBranchNameSuccessfully() {
        Long branchId = 10L;
        String newName = "New North Branch";
        Branch existing = new Branch(branchId, "North Branch");
        Branch updated = new Branch(branchId, newName);

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(existing));
        when(branchRepository.existsByName(newName)).thenReturn(Mono.just(false));
        when(branchRepository.updateName(branchId, newName)).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.execute(branchId, newName))
                .expectNextMatches(result ->
                        result.id().equals(branchId) &&
                        result.name().equals(newName))
                .verifyComplete();

        verify(branchRepository).updateName(branchId, newName);
    }

    @Test
    @DisplayName("Should throw BRANCH_NAME_REQUIRED when name is null")
    void shouldThrowWhenNameIsNull() {
        StepVerifier.create(useCase.execute(10L, null))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.BRANCH_NAME_REQUIRED)
                .verify();

        verify(branchRepository, never()).findById(anyLong());
        verify(branchRepository, never()).updateName(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should throw BRANCH_NAME_REQUIRED when name is blank")
    void shouldThrowWhenNameIsBlank() {
        StepVerifier.create(useCase.execute(10L, "   "))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.BRANCH_NAME_REQUIRED)
                .verify();

        verify(branchRepository, never()).findById(anyLong());
        verify(branchRepository, never()).updateName(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should throw BRANCH_NOT_FOUND when branch does not exist")
    void shouldThrowWhenBranchNotFound() {
        Long branchId = 999L;
        String newName = "New Name";

        when(branchRepository.findById(branchId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(branchId, newName))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.BRANCH_NOT_FOUND)
                .verify();

        verify(branchRepository, never()).existsByName(anyString());
        verify(branchRepository, never()).updateName(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should throw BRANCH_NAME_ALREADY_EXISTS when name is already taken")
    void shouldThrowWhenNameAlreadyExists() {
        Long branchId = 10L;
        String newName = "Existing Name";
        Branch existing = new Branch(branchId, "North Branch");

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(existing));
        when(branchRepository.existsByName(newName)).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(branchId, newName))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.BRANCH_NAME_ALREADY_EXISTS)
                .verify();

        verify(branchRepository, never()).updateName(anyLong(), anyString());
    }
}
