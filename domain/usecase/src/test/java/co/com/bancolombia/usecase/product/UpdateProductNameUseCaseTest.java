package co.com.bancolombia.usecase.product;

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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductNameUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    private UpdateProductNameUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductNameUseCase(productRepository);
    }

    @Test
    @DisplayName("Should update product name successfully when name is valid, product exists and name is unique")
    void shouldUpdateProductNameSuccessfully() {
        Long productId = 10L;
        String newName = "New Laptop Pro";
        Product existing = new Product(productId, "Laptop");
        Product updated = new Product(productId, newName);

        when(productRepository.findById(productId)).thenReturn(Mono.just(existing));
        when(productRepository.existsByName(newName)).thenReturn(Mono.just(false));
        when(productRepository.updateName(productId, newName)).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.execute(productId, newName))
                .expectNextMatches(result ->
                        result.id().equals(productId) &&
                        result.name().equals(newName))
                .verifyComplete();

        verify(productRepository).updateName(productId, newName);
    }

    @Test
    @DisplayName("Should throw PRODUCT_NAME_REQUIRED when name is null")
    void shouldThrowWhenNameIsNull() {
        StepVerifier.create(useCase.execute(10L, null))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.PRODUCT_NAME_REQUIRED)
                .verify();

        verify(productRepository, never()).findById(anyLong());
        verify(productRepository, never()).updateName(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should throw PRODUCT_NAME_REQUIRED when name is blank")
    void shouldThrowWhenNameIsBlank() {
        StepVerifier.create(useCase.execute(10L, "   "))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.PRODUCT_NAME_REQUIRED)
                .verify();

        verify(productRepository, never()).findById(anyLong());
        verify(productRepository, never()).updateName(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should throw PRODUCT_NOT_FOUND when product does not exist")
    void shouldThrowWhenProductNotFound() {
        Long productId = 999L;
        String newName = "New Name";

        when(productRepository.findById(productId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(productId, newName))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.PRODUCT_NOT_FOUND)
                .verify();

        verify(productRepository, never()).existsByName(anyString());
        verify(productRepository, never()).updateName(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should throw PRODUCT_NAME_ALREADY_EXISTS when name is already taken")
    void shouldThrowWhenNameAlreadyExists() {
        Long productId = 10L;
        String newName = "Existing Product";
        Product existing = new Product(productId, "Laptop");

        when(productRepository.findById(productId)).thenReturn(Mono.just(existing));
        when(productRepository.existsByName(newName)).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(productId, newName))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.PRODUCT_NAME_ALREADY_EXISTS)
                .verify();

        verify(productRepository, never()).updateName(anyLong(), anyString());
    }
}
