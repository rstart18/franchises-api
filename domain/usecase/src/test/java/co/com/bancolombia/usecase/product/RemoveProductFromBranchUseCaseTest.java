package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateway.BranchRepository;
import co.com.bancolombia.model.branchproduct.BranchProduct;
import co.com.bancolombia.model.branchproduct.gateway.BranchProductRepository;
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveProductFromBranchUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchProductRepository branchProductRepository;

    private RemoveProductFromBranchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RemoveProductFromBranchUseCase(branchRepository, branchProductRepository);
    }

    @Test
    @DisplayName("Should soft delete product from branch successfully")
    void shouldSoftDeleteProductFromBranchSuccessfully() {
        // Given
        Long branchId = 1L;
        Long productId = 10L;

        Branch branch = new Branch(branchId, "Main Branch");
        BranchProduct branchProduct = new BranchProduct(productId, branchId, "Laptop", 50);

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(branchProductRepository.findActiveByBranchAndProduct(branchId, productId))
                .thenReturn(Mono.just(branchProduct));
        when(branchProductRepository.softDelete(branchId, productId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productId))
                .verifyComplete();

        verify(branchRepository).findById(branchId);
        verify(branchProductRepository).findActiveByBranchAndProduct(branchId, productId);
        verify(branchProductRepository).softDelete(branchId, productId);
    }

    @Test
    @DisplayName("Should fail with BRANCH_NOT_FOUND when branch does not exist")
    void shouldFailWhenBranchNotFound() {
        // Given
        Long branchId = 99L;
        Long productId = 10L;

        when(branchRepository.findById(branchId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productId))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.BRANCH_NOT_FOUND)
                .verify();

        verify(branchRepository).findById(branchId);
        verify(branchProductRepository, never()).findActiveByBranchAndProduct(branchId, productId);
        verify(branchProductRepository, never()).softDelete(branchId, productId);
    }

    @Test
    @DisplayName("Should fail with BRANCH_PRODUCT_NOT_FOUND when product is not associated with the branch")
    void shouldFailWhenProductNotAssociatedWithBranch() {
        // Given
        Long branchId = 1L;
        Long productId = 99L;

        Branch branch = new Branch(branchId, "Main Branch");

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(branchProductRepository.findActiveByBranchAndProduct(branchId, productId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productId))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.BRANCH_PRODUCT_NOT_FOUND)
                .verify();

        verify(branchRepository).findById(branchId);
        verify(branchProductRepository).findActiveByBranchAndProduct(branchId, productId);
        verify(branchProductRepository, never()).softDelete(branchId, productId);
    }
}
