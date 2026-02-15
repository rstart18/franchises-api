package co.com.bancolombia.adapter.product;

import co.com.bancolombia.model.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterTest {

    @Mock
    private ProductR2dbcRepository productR2dbcRepository;

    @Mock
    private ProductDataMapper mapper;

    private ProductRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProductRepositoryAdapter(productR2dbcRepository, mapper);
    }

    @Test
    @DisplayName("Should find product by name and return domain object")
    void shouldFindProductByName() {
        ProductData data = ProductData.builder().id(10L).name("Laptop").build();
        Product domainResult = new Product(10L, "Laptop");

        when(productR2dbcRepository.findByName("Laptop")).thenReturn(Mono.just(data));
        when(mapper.toDomain(data)).thenReturn(domainResult);

        StepVerifier.create(adapter.findByName("Laptop"))
                .expectNextMatches(result ->
                        result.id().equals(10L) &&
                        result.name().equals("Laptop"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when product not found by name")
    void shouldReturnEmptyWhenProductNotFoundByName() {
        when(productR2dbcRepository.findByName("Unknown")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByName("Unknown"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should save product and return domain object")
    void shouldSaveProduct() {
        Product product = new Product(null, "Laptop");
        ProductData data = ProductData.builder().name("Laptop").build();
        ProductData savedData = ProductData.builder().id(10L).name("Laptop").build();
        Product domainResult = new Product(10L, "Laptop");

        when(mapper.toData(product)).thenReturn(data);
        when(productR2dbcRepository.save(data)).thenReturn(Mono.just(savedData));
        when(mapper.toDomain(savedData)).thenReturn(domainResult);

        StepVerifier.create(adapter.save(product))
                .expectNextMatches(result ->
                        result.id().equals(10L) &&
                        result.name().equals("Laptop"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find product by id and return domain object")
    void shouldFindProductById() {
        Long id = 10L;
        ProductData data = ProductData.builder().id(id).name("Laptop").build();
        Product domainResult = new Product(id, "Laptop");

        when(productR2dbcRepository.findById(id)).thenReturn(Mono.just(data));
        when(mapper.toDomain(data)).thenReturn(domainResult);

        StepVerifier.create(adapter.findById(id))
                .expectNextMatches(result ->
                        result.id().equals(id) &&
                        result.name().equals("Laptop"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when product not found by id")
    void shouldReturnEmptyWhenProductNotFoundById() {
        when(productR2dbcRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(999L))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return true when product name exists")
    void shouldReturnTrueWhenProductNameExists() {
        when(productR2dbcRepository.existsByName("Laptop")).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsByName("Laptop"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return false when product name does not exist")
    void shouldReturnFalseWhenProductNameDoesNotExist() {
        when(productR2dbcRepository.existsByName(anyString())).thenReturn(Mono.just(false));

        StepVerifier.create(adapter.existsByName("New Product"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update product name and return domain object")
    void shouldUpdateProductName() {
        Long id = 10L;
        String newName = "Laptop Pro";
        ProductData existingData = ProductData.builder().id(id).name("Laptop").build();
        ProductData savedData = ProductData.builder().id(id).name(newName).build();
        Product domainResult = new Product(id, newName);

        when(productR2dbcRepository.findById(id)).thenReturn(Mono.just(existingData));
        when(productR2dbcRepository.save(existingData)).thenReturn(Mono.just(savedData));
        when(mapper.toDomain(savedData)).thenReturn(domainResult);

        StepVerifier.create(adapter.updateName(id, newName))
                .expectNextMatches(result ->
                        result.id().equals(id) &&
                        result.name().equals(newName))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when updating name of non-existent product")
    void shouldReturnEmptyWhenUpdatingNameOfNonExistentProduct() {
        when(productR2dbcRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.updateName(999L, "New Name"))
                .verifyComplete();
    }
}
