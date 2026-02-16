package co.com.bancolombia.adapter.branchproduct;

import co.com.bancolombia.adapter.product.ProductData;
import co.com.bancolombia.adapter.product.ProductR2dbcRepository;
import co.com.bancolombia.model.branchproduct.BranchProduct;
import co.com.bancolombia.model.branchproduct.TopStockProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchProductRepositoryAdapterTest {

    @Mock
    private BranchProductR2dbcRepository branchProductR2dbcRepository;

    @Mock
    private ProductR2dbcRepository productR2dbcRepository;

    @Mock
    private BranchProductDataMapper mapper;

    private BranchProductRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BranchProductRepositoryAdapter(branchProductR2dbcRepository, productR2dbcRepository, mapper);
    }

    @Test
    @DisplayName("Should save branch product and return domain object with product name")
    void shouldSaveBranchProduct() {
        // Given
        BranchProduct branchProduct = new BranchProduct(10L, 1L, "Laptop", 50);
        BranchProductData data = BranchProductData.builder().productId(10L).branchId(1L).stock(50).build();
        BranchProductData savedData = BranchProductData.builder().productId(10L).branchId(1L).stock(50).build();
        ProductData productData = ProductData.builder().id(10L).name("Laptop").build();
        BranchProduct expectedResult = new BranchProduct(10L, 1L, "Laptop", 50);

        when(mapper.toData(branchProduct)).thenReturn(data);
        when(branchProductR2dbcRepository.save(data)).thenReturn(Mono.just(savedData));
        when(productR2dbcRepository.findById(10L)).thenReturn(Mono.just(productData));
        when(mapper.toDomain(savedData, "Laptop")).thenReturn(expectedResult);

        // When & Then
        StepVerifier.create(adapter.save(branchProduct))
                .expectNextMatches(result ->
                        result.productId().equals(10L) &&
                        result.branchId().equals(1L) &&
                        result.productName().equals("Laptop") &&
                        result.stock().equals(50))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find branch product by branch and product IDs")
    void shouldFindByBranchAndProduct() {
        // Given
        Long branchId = 1L;
        Long productId = 10L;
        BranchProductData data = BranchProductData.builder().productId(productId).branchId(branchId).stock(50).build();
        ProductData productData = ProductData.builder().id(productId).name("Laptop").build();
        BranchProduct expectedResult = new BranchProduct(productId, branchId, "Laptop", 50);

        when(branchProductR2dbcRepository.findByBranchAndProduct(branchId, productId)).thenReturn(Mono.just(data));
        when(productR2dbcRepository.findById(productId)).thenReturn(Mono.just(productData));
        when(mapper.toDomain(data, "Laptop")).thenReturn(expectedResult);

        // When & Then
        StepVerifier.create(adapter.findByBranchAndProduct(branchId, productId))
                .expectNextMatches(result ->
                        result.productId().equals(productId) &&
                        result.branchId().equals(branchId) &&
                        result.productName().equals("Laptop"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when branch product not found")
    void shouldReturnEmptyWhenNotFound() {
        // Given
        Long branchId = 1L;
        Long productId = 99L;

        when(branchProductR2dbcRepository.findByBranchAndProduct(branchId, productId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(adapter.findByBranchAndProduct(branchId, productId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should soft delete branch product association")
    void shouldSoftDeleteBranchProduct() {
        // Given
        Long branchId = 1L;
        Long productId = 10L;

        when(branchProductR2dbcRepository.softDelete(branchId, productId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(adapter.softDelete(branchId, productId))
                .verifyComplete();

        verify(branchProductR2dbcRepository).softDelete(branchId, productId);
    }

    @Test
    @DisplayName("Should find active branch product by branch and product IDs")
    void shouldFindActiveByBranchAndProduct() {
        // Given
        Long branchId = 1L;
        Long productId = 10L;
        BranchProductData data = BranchProductData.builder().productId(productId).branchId(branchId).stock(50).build();
        ProductData productData = ProductData.builder().id(productId).name("Laptop").build();
        BranchProduct expectedResult = new BranchProduct(productId, branchId, "Laptop", 50);

        when(branchProductR2dbcRepository.findActiveByBranchAndProduct(branchId, productId)).thenReturn(Mono.just(data));
        when(productR2dbcRepository.findById(productId)).thenReturn(Mono.just(productData));
        when(mapper.toDomain(data, "Laptop")).thenReturn(expectedResult);

        // When & Then
        StepVerifier.create(adapter.findActiveByBranchAndProduct(branchId, productId))
                .expectNextMatches(result ->
                        result.productId().equals(productId) &&
                        result.branchId().equals(branchId) &&
                        result.productName().equals("Laptop") &&
                        result.stock().equals(50))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when no active branch product found")
    void shouldReturnEmptyWhenNoActiveFound() {
        // Given
        Long branchId = 1L;
        Long productId = 10L;

        when(branchProductR2dbcRepository.findActiveByBranchAndProduct(branchId, productId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(adapter.findActiveByBranchAndProduct(branchId, productId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update stock and return updated branch product")
    void shouldUpdateStock() {
        // Given
        Long branchId = 1L;
        Long productId = 10L;
        Integer newStock = 75;
        BranchProductData existingData = BranchProductData.builder().productId(productId).branchId(branchId).stock(50).build();
        BranchProductData savedData = BranchProductData.builder().productId(productId).branchId(branchId).stock(newStock).build();
        ProductData productData = ProductData.builder().id(productId).name("Laptop").build();
        BranchProduct expectedResult = new BranchProduct(productId, branchId, "Laptop", newStock);

        when(branchProductR2dbcRepository.findActiveByBranchAndProduct(branchId, productId)).thenReturn(Mono.just(existingData));
        when(branchProductR2dbcRepository.save(existingData)).thenReturn(Mono.just(savedData));
        when(productR2dbcRepository.findById(productId)).thenReturn(Mono.just(productData));
        when(mapper.toDomain(savedData, "Laptop")).thenReturn(expectedResult);

        // When & Then
        StepVerifier.create(adapter.updateStock(branchId, productId, newStock))
                .expectNextMatches(result ->
                        result.productId().equals(productId) &&
                        result.branchId().equals(branchId) &&
                        result.productName().equals("Laptop") &&
                        result.stock().equals(newStock))
                .verifyComplete();

        verify(branchProductR2dbcRepository).findActiveByBranchAndProduct(branchId, productId);
        verify(branchProductR2dbcRepository).save(existingData);
    }

    @Test
    @DisplayName("Should find top stock products by franchise")
    void shouldFindTopStockByFranchise() {
        // Given
        Long franchiseId = 1L;
        TopStockProductProjection projection1 = new TopStockProductProjection(100L, "Laptop", 150, 10L, "North Branch");
        TopStockProductProjection projection2 = new TopStockProductProjection(200L, "Phone", 80, 20L, "South Branch");
        TopStockProduct topStock1 = new TopStockProduct(100L, "Laptop", 150, 10L, "North Branch");
        TopStockProduct topStock2 = new TopStockProduct(200L, "Phone", 80, 20L, "South Branch");

        when(branchProductR2dbcRepository.findTopStockByFranchise(franchiseId)).thenReturn(Flux.just(projection1, projection2));
        when(mapper.toDomain(projection1)).thenReturn(topStock1);
        when(mapper.toDomain(projection2)).thenReturn(topStock2);

        // When & Then
        StepVerifier.create(adapter.findTopStockByFranchise(franchiseId))
                .expectNextMatches(result ->
                        result.productId().equals(100L) &&
                        result.productName().equals("Laptop") &&
                        result.stock().equals(150) &&
                        result.branchId().equals(10L) &&
                        result.branchName().equals("North Branch"))
                .expectNextMatches(result ->
                        result.productId().equals(200L) &&
                        result.productName().equals("Phone") &&
                        result.stock().equals(80) &&
                        result.branchId().equals(20L) &&
                        result.branchName().equals("South Branch"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when no products in franchise for top stock")
    void shouldReturnEmptyWhenNoProductsInFranchiseForTopStock() {
        // Given
        Long franchiseId = 1L;

        when(branchProductR2dbcRepository.findTopStockByFranchise(franchiseId)).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(adapter.findTopStockByFranchise(franchiseId))
                .verifyComplete();
    }
}
