package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateway.BranchRepository;
import co.com.bancolombia.model.branchproduct.BranchProduct;
import co.com.bancolombia.model.branchproduct.StockAlert;
import co.com.bancolombia.model.branchproduct.gateway.BranchProductRepository;
import co.com.bancolombia.model.branchproduct.gateway.StockAlertGateway;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductStockUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchProductRepository branchProductRepository;

    @Mock
    private StockAlertGateway stockAlertGateway;

    private UpdateProductStockUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductStockUseCase(branchRepository, branchProductRepository, stockAlertGateway);
    }

    @Test
    @DisplayName("Should update stock successfully without alert when stock is above threshold")
    void shouldUpdateStockSuccessfully() {
        // Given
        Long branchId = 1L;
        Long productId = 10L;
        Integer newStock = 75;

        Branch branch = new Branch(branchId, "Main Branch");
        BranchProduct activeBp = new BranchProduct(productId, branchId, "Laptop", 50);
        BranchProduct updatedBp = new BranchProduct(productId, branchId, "Laptop", newStock);

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(branchProductRepository.findActiveByBranchAndProduct(branchId, productId))
                .thenReturn(Mono.just(activeBp));
        when(branchProductRepository.updateStock(branchId, productId, newStock))
                .thenReturn(Mono.just(updatedBp));

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productId, newStock))
                .expectNextMatches(result ->
                        result.productId().equals(productId) &&
                        result.branchId().equals(branchId) &&
                        result.productName().equals("Laptop") &&
                        result.stock().equals(newStock))
                .verifyComplete();

        verify(branchRepository).findById(branchId);
        verify(branchProductRepository).findActiveByBranchAndProduct(branchId, productId);
        verify(branchProductRepository).updateStock(branchId, productId, newStock);
        verify(stockAlertGateway, never()).sendAlert(any());
    }

    @Test
    @DisplayName("Should update stock to zero and send low-stock alert")
    void shouldUpdateStockToZeroSuccessfully() {
        // Given
        Long branchId = 1L;
        Long productId = 10L;
        Integer newStock = 0;

        Branch branch = new Branch(branchId, "Main Branch");
        BranchProduct activeBp = new BranchProduct(productId, branchId, "Laptop", 50);
        BranchProduct updatedBp = new BranchProduct(productId, branchId, "Laptop", newStock);

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(branchProductRepository.findActiveByBranchAndProduct(branchId, productId))
                .thenReturn(Mono.just(activeBp));
        when(branchProductRepository.updateStock(branchId, productId, newStock))
                .thenReturn(Mono.just(updatedBp));
        when(stockAlertGateway.sendAlert(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productId, newStock))
                .expectNextMatches(result -> result.stock().equals(0))
                .verifyComplete();

        verify(stockAlertGateway).sendAlert(any());
    }

    @Test
    @DisplayName("Should fail with BRANCH_NOT_FOUND when branch does not exist")
    void shouldFailWhenBranchNotFound() {
        // Given
        Long branchId = 99L;
        Long productId = 10L;
        Integer newStock = 75;

        when(branchRepository.findById(branchId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productId, newStock))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.BRANCH_NOT_FOUND)
                .verify();

        verify(branchRepository).findById(branchId);
        verify(branchProductRepository, never()).findActiveByBranchAndProduct(branchId, productId);
        verify(branchProductRepository, never()).updateStock(branchId, productId, newStock);
    }

    @Test
    @DisplayName("Should fail with BRANCH_PRODUCT_NOT_FOUND when product is not associated with branch")
    void shouldFailWhenProductNotAssociatedWithBranch() {
        // Given
        Long branchId = 1L;
        Long productId = 99L;
        Integer newStock = 75;

        Branch branch = new Branch(branchId, "Main Branch");

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(branchProductRepository.findActiveByBranchAndProduct(branchId, productId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productId, newStock))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.BRANCH_PRODUCT_NOT_FOUND)
                .verify();

        verify(branchRepository).findById(branchId);
        verify(branchProductRepository).findActiveByBranchAndProduct(branchId, productId);
        verify(branchProductRepository, never()).updateStock(branchId, productId, newStock);
    }

    @Test
    @DisplayName("Should fail with PRODUCT_STOCK_INVALID when stock is null")
    void shouldFailWhenStockIsNull() {
        // Given
        Long branchId = 1L;
        Long productId = 10L;
        Integer newStock = null;

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productId, newStock))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.PRODUCT_STOCK_INVALID)
                .verify();

        verify(branchRepository, never()).findById(branchId);
        verify(branchProductRepository, never()).findActiveByBranchAndProduct(branchId, productId);
        verify(branchProductRepository, never()).updateStock(branchId, productId, newStock);
    }

    @Test
    @DisplayName("Should fail with PRODUCT_STOCK_INVALID when stock is negative")
    void shouldFailWhenStockIsNegative() {
        // Given
        Long branchId = 1L;
        Long productId = 10L;
        Integer newStock = -5;

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productId, newStock))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.PRODUCT_STOCK_INVALID)
                .verify();

        verify(branchRepository, never()).findById(branchId);
        verify(branchProductRepository, never()).findActiveByBranchAndProduct(branchId, productId);
        verify(branchProductRepository, never()).updateStock(branchId, productId, newStock);
    }

    // ── Low-Stock Alert Tests ──────────────────────────────────────────────────

    @Test
    @DisplayName("Should send low-stock alert when stock is below threshold")
    void shouldSendAlertWhenStockBelowThreshold() {
        // Given
        Long branchId = 1L;
        Long productId = 10L;
        Integer newStock = 3;

        Branch branch = new Branch(branchId, "Main Branch");
        BranchProduct activeBp = new BranchProduct(productId, branchId, "Laptop", 50);
        BranchProduct updatedBp = new BranchProduct(productId, branchId, "Laptop", newStock);

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(branchProductRepository.findActiveByBranchAndProduct(branchId, productId))
                .thenReturn(Mono.just(activeBp));
        when(branchProductRepository.updateStock(branchId, productId, newStock))
                .thenReturn(Mono.just(updatedBp));
        when(stockAlertGateway.sendAlert(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productId, newStock))
                .expectNextMatches(result -> result.stock().equals(newStock))
                .verifyComplete();

        verify(stockAlertGateway).sendAlert(argThat(alert ->
                alert.productId().equals(productId) &&
                alert.branchId().equals(branchId) &&
                alert.productName().equals("Laptop") &&
                alert.currentStock().equals(newStock)));
    }

    @Test
    @DisplayName("Should not send alert when stock is exactly at threshold")
    void shouldNotSendAlertWhenStockAtThreshold() {
        // Given
        Long branchId = 1L;
        Long productId = 10L;
        Integer newStock = 5;

        Branch branch = new Branch(branchId, "Main Branch");
        BranchProduct activeBp = new BranchProduct(productId, branchId, "Laptop", 50);
        BranchProduct updatedBp = new BranchProduct(productId, branchId, "Laptop", newStock);

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(branchProductRepository.findActiveByBranchAndProduct(branchId, productId))
                .thenReturn(Mono.just(activeBp));
        when(branchProductRepository.updateStock(branchId, productId, newStock))
                .thenReturn(Mono.just(updatedBp));

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productId, newStock))
                .expectNextMatches(result -> result.stock().equals(newStock))
                .verifyComplete();

        verify(stockAlertGateway, never()).sendAlert(any());
    }

    @Test
    @DisplayName("Should complete stock update even when alert fails (graceful degradation)")
    void shouldCompleteStockUpdateEvenWhenAlertFails() {
        // Given
        Long branchId = 1L;
        Long productId = 10L;
        Integer newStock = 2;

        Branch branch = new Branch(branchId, "Main Branch");
        BranchProduct activeBp = new BranchProduct(productId, branchId, "Laptop", 50);
        BranchProduct updatedBp = new BranchProduct(productId, branchId, "Laptop", newStock);

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(branchProductRepository.findActiveByBranchAndProduct(branchId, productId))
                .thenReturn(Mono.just(activeBp));
        when(branchProductRepository.updateStock(branchId, productId, newStock))
                .thenReturn(Mono.just(updatedBp));
        when(stockAlertGateway.sendAlert(any()))
                .thenReturn(Mono.error(new RuntimeException("Notification service unavailable")));

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productId, newStock))
                .expectNextMatches(result ->
                        result.stock().equals(newStock) &&
                        result.productId().equals(productId))
                .verifyComplete();

        verify(stockAlertGateway).sendAlert(any());
    }
}
