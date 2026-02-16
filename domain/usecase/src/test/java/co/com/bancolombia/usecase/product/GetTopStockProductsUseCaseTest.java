package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.branchproduct.TopStockProduct;
import co.com.bancolombia.model.branchproduct.gateway.BranchProductRepository;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class GetTopStockProductsUseCaseTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchProductRepository branchProductRepository;

    private GetTopStockProductsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetTopStockProductsUseCase(franchiseRepository, branchProductRepository);
    }

    @Test
    @DisplayName("Should return top stock product for each branch of a franchise")
    void shouldReturnTopStockProductsForFranchise() {
        Long franchiseId = 1L;
        Franchise franchise = Franchise.builder().id(franchiseId).name("Burger Kingdom").build();
        TopStockProduct tp1 = new TopStockProduct(100L, "Laptop", 150, 10L, "North Branch");
        TopStockProduct tp2 = new TopStockProduct(200L, "Phone", 80, 20L, "South Branch");

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));
        when(branchProductRepository.findTopStockByFranchise(franchiseId)).thenReturn(Flux.just(tp1, tp2));

        StepVerifier.create(useCase.execute(franchiseId))
                .expectNextMatches(tp -> tp.productId().equals(100L) &&
                        tp.productName().equals("Laptop") &&
                        tp.stock().equals(150) &&
                        tp.branchId().equals(10L) &&
                        tp.branchName().equals("North Branch"))
                .expectNextMatches(tp -> tp.productId().equals(200L) &&
                        tp.productName().equals("Phone") &&
                        tp.stock().equals(80) &&
                        tp.branchId().equals(20L) &&
                        tp.branchName().equals("South Branch"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty flux when franchise has no branches with products")
    void shouldReturnEmptyWhenNoBranchesWithProducts() {
        Long franchiseId = 1L;
        Franchise franchise = Franchise.builder().id(franchiseId).name("Burger Kingdom").build();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));
        when(branchProductRepository.findTopStockByFranchise(franchiseId)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(franchiseId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw FRANCHISE_NOT_FOUND when franchise does not exist")
    void shouldThrowWhenFranchiseNotFound() {
        Long franchiseId = 999L;

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(franchiseId))
                .expectErrorMatches(error -> error instanceof BusinessException &&
                        ((BusinessException) error).getErrorCode() == DomainErrorCode.FRANCHISE_NOT_FOUND)
                .verify();

        verify(branchProductRepository, never()).findTopStockByFranchise(anyLong());
    }
}
