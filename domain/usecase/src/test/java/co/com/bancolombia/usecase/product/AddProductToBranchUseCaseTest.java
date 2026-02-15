package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateway.BranchRepository;
import co.com.bancolombia.model.branchproduct.BranchProduct;
import co.com.bancolombia.model.branchproduct.gateway.BranchProductRepository;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.product.Product;
import co.com.bancolombia.model.product.gateway.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddProductToBranchUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BranchProductRepository branchProductRepository;

    private AddProductToBranchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AddProductToBranchUseCase(branchRepository, productRepository, branchProductRepository);
    }

    @Test
    @DisplayName("Should add NEW product to branch successfully when branch exists and product is valid")
    void shouldAddNewProductToBranchSuccessfully() {
        // Given
        Long branchId = 1L;
        String productName = "Laptop";
        Integer stock = 50;

        Branch branch = new Branch(branchId, "Main Branch");
        Product newProduct = new Product(10L, productName);
        BranchProduct expectedBranchProduct = new BranchProduct(10L, branchId, productName, stock);

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(productRepository.findByName(productName)).thenReturn(Mono.empty());
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(newProduct));
        when(branchProductRepository.save(any(BranchProduct.class))).thenReturn(Mono.just(expectedBranchProduct));

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productName, stock))
                .expectNextMatches(result ->
                        result.productId().equals(10L) &&
                        result.branchId().equals(branchId) &&
                        result.productName().equals(productName) &&
                        result.stock().equals(stock)
                )
                .verifyComplete();

        verify(branchRepository).findById(branchId);
        verify(productRepository).findByName(productName);
        verify(productRepository).save(any(Product.class));
        verify(branchProductRepository).save(any(BranchProduct.class));
    }

    @Test
    @DisplayName("Should add EXISTING product to different branch successfully")
    void shouldAddExistingProductToDifferentBranch() {
        // Given
        Long branchId = 2L;
        String productName = "Laptop";
        Integer stock = 30;

        Branch branch = new Branch(branchId, "Secondary Branch");
        Product existingProduct = new Product(10L, productName);
        BranchProduct expectedBranchProduct = new BranchProduct(10L, branchId, productName, stock);

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(productRepository.findByName(productName)).thenReturn(Mono.just(existingProduct));
        when(branchProductRepository.save(any(BranchProduct.class))).thenReturn(Mono.just(expectedBranchProduct));

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productName, stock))
                .expectNextMatches(result ->
                        result.productId().equals(10L) &&
                        result.branchId().equals(branchId) &&
                        result.productName().equals(productName) &&
                        result.stock().equals(stock)
                )
                .verifyComplete();

        verify(branchRepository).findById(branchId);
        verify(productRepository).findByName(productName);
        verify(productRepository, never()).save(any(Product.class));
        verify(branchProductRepository).save(any(BranchProduct.class));
    }

    @Test
    @DisplayName("Should fail with BRANCH_NOT_FOUND when branch does not exist")
    void shouldFailWhenBranchNotFound() {
        // Given
        Long branchId = 99L;
        String productName = "Laptop";
        Integer stock = 50;

        when(branchRepository.findById(branchId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productName, stock))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.BRANCH_NOT_FOUND)
                .verify();

        verify(branchRepository).findById(branchId);
        verify(productRepository, never()).findByName(any());
        verify(productRepository, never()).save(any());
        verify(branchProductRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail with PRODUCT_NAME_REQUIRED when name is null")
    void shouldFailWhenProductNameIsNull() {
        // Given
        Long branchId = 1L;
        String productName = null;
        Integer stock = 50;

        Branch branch = new Branch(branchId, "Main Branch");
        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productName, stock))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.PRODUCT_NAME_REQUIRED)
                .verify();

        verify(branchRepository).findById(branchId);
        verify(productRepository, never()).findByName(any());
        verify(productRepository, never()).save(any());
        verify(branchProductRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail with PRODUCT_NAME_REQUIRED when name is blank")
    void shouldFailWhenProductNameIsBlank() {
        // Given
        Long branchId = 1L;
        String productName = "   ";
        Integer stock = 50;

        Branch branch = new Branch(branchId, "Main Branch");
        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productName, stock))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.PRODUCT_NAME_REQUIRED)
                .verify();

        verify(branchRepository).findById(branchId);
        verify(productRepository, never()).findByName(any());
        verify(productRepository, never()).save(any());
        verify(branchProductRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail with PRODUCT_STOCK_INVALID when stock is null")
    void shouldFailWhenStockIsNull() {
        // Given
        Long branchId = 1L;
        String productName = "Laptop";
        Integer stock = null;

        Branch branch = new Branch(branchId, "Main Branch");
        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productName, stock))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.PRODUCT_STOCK_INVALID)
                .verify();

        verify(branchRepository).findById(branchId);
        verify(productRepository, never()).findByName(any());
        verify(productRepository, never()).save(any());
        verify(branchProductRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail with PRODUCT_STOCK_INVALID when stock is negative")
    void shouldFailWhenStockIsNegative() {
        // Given
        Long branchId = 1L;
        String productName = "Laptop";
        Integer stock = -5;

        Branch branch = new Branch(branchId, "Main Branch");
        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));

        // When & Then
        StepVerifier.create(useCase.execute(branchId, productName, stock))
                .expectErrorMatches(e -> e instanceof BusinessException be &&
                        be.getErrorCode() == DomainErrorCode.PRODUCT_STOCK_INVALID)
                .verify();

        verify(branchRepository).findById(branchId);
        verify(productRepository, never()).findByName(any());
        verify(productRepository, never()).save(any());
        verify(branchProductRepository, never()).save(any());
    }
}
