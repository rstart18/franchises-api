package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.config.RequestValidator;
import co.com.bancolombia.api.dto.BranchProductResponse;
import co.com.bancolombia.api.dto.BranchResponse;
import co.com.bancolombia.api.dto.FranchiseResponse;
import co.com.bancolombia.api.mapper.BranchMapper;
import co.com.bancolombia.api.mapper.BranchProductMapper;
import co.com.bancolombia.api.mapper.FranchiseMapper;
import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branchproduct.BranchProduct;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.usecase.franchise.AddBranchToFranchiseUseCase;
import co.com.bancolombia.usecase.franchise.CreateFranchiseUseCase;
import co.com.bancolombia.usecase.product.AddProductToBranchUseCase;
import co.com.bancolombia.usecase.product.RemoveProductFromBranchUseCase;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseRouterTest {

    @Mock
    private CreateFranchiseUseCase createFranchiseUseCase;

    @Mock
    private FranchiseMapper franchiseMapper;

    @Mock
    private AddBranchToFranchiseUseCase addBranchToFranchiseUseCase;

    @Mock
    private BranchMapper branchMapper;

    @Mock
    private AddProductToBranchUseCase addProductToBranchUseCase;

    @Mock
    private BranchProductMapper branchProductMapper;

    @Mock
    private RemoveProductFromBranchUseCase removeProductFromBranchUseCase;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        RequestValidator requestValidator = new RequestValidator(validator);
        FranchiseHandler franchiseHandler = new FranchiseHandler(
                createFranchiseUseCase, franchiseMapper, requestValidator,
                addBranchToFranchiseUseCase, branchMapper, addProductToBranchUseCase,
                branchProductMapper, removeProductFromBranchUseCase);
        FranchiseRouter franchiseRouter = new FranchiseRouter();

        webTestClient = WebTestClient.bindToRouterFunction(franchiseRouter.franchiseRoutes(franchiseHandler))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/franchises should return 201 with the created franchise")
    void shouldReturn201WhenFranchiseCreated() {
        Franchise domainFranchise = Franchise.builder().name("Burger Kingdom").build();
        Franchise savedFranchise = Franchise.builder().id(1L).name("Burger Kingdom").build();
        FranchiseResponse response = new FranchiseResponse(1L, "Burger Kingdom");

        when(franchiseMapper.toDomain(any())).thenReturn(domainFranchise);
        when(createFranchiseUseCase.execute(any())).thenReturn(Mono.just(savedFranchise));
        when(franchiseMapper.toResponse(savedFranchise)).thenReturn(response);

        webTestClient.post()
                .uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Burger Kingdom\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(FranchiseResponse.class)
                .value(r -> {
                    assert r.id().equals(1L);
                    assert r.name().equals("Burger Kingdom");
                });
    }

    @Test
    @DisplayName("POST /api/v1/franchises should propagate error through doOnError when use case fails")
    void shouldPropagateErrorWhenUseCaseFails() {
        when(franchiseMapper.toDomain(any())).thenReturn(Franchise.builder().name("Burger Kingdom").build());
        when(createFranchiseUseCase.execute(any()))
                .thenReturn(Mono.error(new BusinessException(DomainErrorCode.FRANCHISE_NAME_ALREADY_EXISTS)));

        webTestClient.post()
                .uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Burger Kingdom\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("POST /api/v1/franchises/{id}/branches should return 201 with the created branch")
    void shouldReturn201WhenBranchAddedToFranchise() {
        Branch domainBranch = new Branch(null, "North Branch");
        Branch savedBranch = new Branch(10L, "North Branch");
        BranchResponse response = new BranchResponse(10L, "North Branch");

        when(branchMapper.toDomain(any())).thenReturn(domainBranch);
        when(addBranchToFranchiseUseCase.execute(eq(1L), any())).thenReturn(Mono.just(savedBranch));
        when(branchMapper.toResponse(savedBranch)).thenReturn(response);

        webTestClient.post()
                .uri("/api/v1/franchises/1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"North Branch\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BranchResponse.class)
                .value(r -> {
                    assert r.id().equals(10L);
                    assert r.name().equals("North Branch");
                });
    }

    @Test
    @DisplayName("POST /api/v1/franchises/{id}/branches should propagate error when franchise not found")
    void shouldPropagateErrorWhenFranchiseNotFoundForBranch() {
        when(branchMapper.toDomain(any())).thenReturn(new Branch(null, "North Branch"));
        when(addBranchToFranchiseUseCase.execute(any(), any()))
                .thenReturn(Mono.error(new BusinessException(DomainErrorCode.FRANCHISE_NOT_FOUND)));

        webTestClient.post()
                .uri("/api/v1/franchises/99/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"North Branch\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("POST /api/v1/franchises/{id}/branches should return 500 when branch name is blank (no GlobalWebExceptionHandler in unit test)")
    void shouldReturn500WhenBranchNameIsBlank() {
        webTestClient.post()
                .uri("/api/v1/franchises/1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("POST /api/v1/branches/{id}/products should return 201 with created branch-product")
    void shouldReturn201WhenProductAddedToBranch() {
        Long branchId = 1L;
        String productName = "Laptop";
        Integer stock = 50;
        BranchProduct branchProduct = new BranchProduct(10L, branchId, productName, stock);
        BranchProductResponse response = new BranchProductResponse(10L, branchId, productName, stock);

        when(addProductToBranchUseCase.execute(eq(branchId), eq(productName), eq(stock)))
                .thenReturn(Mono.just(branchProduct));
        when(branchProductMapper.toResponse(branchProduct)).thenReturn(response);

        webTestClient.post()
                .uri("/api/v1/branches/1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Laptop\",\"stock\":50}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BranchProductResponse.class)
                .value(r -> {
                    assert r.productId().equals(10L);
                    assert r.branchId().equals(branchId);
                    assert r.productName().equals(productName);
                    assert r.stock().equals(stock);
                });
    }

    @Test
    @DisplayName("POST /api/v1/branches/{id}/products should propagate error when branch not found")
    void shouldPropagateErrorWhenBranchNotFoundForProduct() {
        when(addProductToBranchUseCase.execute(any(), any(), any()))
                .thenReturn(Mono.error(new BusinessException(DomainErrorCode.BRANCH_NOT_FOUND)));

        webTestClient.post()
                .uri("/api/v1/branches/999/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Laptop\",\"stock\":50}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("POST /api/v1/branches/{id}/products should return 500 when product name is blank")
    void shouldReturn500WhenProductNameIsBlank() {
        webTestClient.post()
                .uri("/api/v1/branches/1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\",\"stock\":50}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("POST /api/v1/branches/{id}/products should return 500 when product stock is negative")
    void shouldReturn500WhenProductStockIsNegative() {
        webTestClient.post()
                .uri("/api/v1/branches/1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Laptop\",\"stock\":-5}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("POST /api/v1/branches/{id}/products should allow adding same product to different branches")
    void shouldAllowAddingSameProductToDifferentBranches() {
        Long branchId1 = 1L;
        Long branchId2 = 2L;
        String productName = "Laptop";
        BranchProduct branchProduct1 = new BranchProduct(10L, branchId1, productName, 50);
        BranchProduct branchProduct2 = new BranchProduct(10L, branchId2, productName, 30);
        BranchProductResponse response1 = new BranchProductResponse(10L, branchId1, productName, 50);
        BranchProductResponse response2 = new BranchProductResponse(10L, branchId2, productName, 30);

        when(addProductToBranchUseCase.execute(eq(branchId1), eq(productName), eq(50)))
                .thenReturn(Mono.just(branchProduct1));
        when(addProductToBranchUseCase.execute(eq(branchId2), eq(productName), eq(30)))
                .thenReturn(Mono.just(branchProduct2));
        when(branchProductMapper.toResponse(branchProduct1)).thenReturn(response1);
        when(branchProductMapper.toResponse(branchProduct2)).thenReturn(response2);

        // Add to first branch
        webTestClient.post()
                .uri("/api/v1/branches/1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Laptop\",\"stock\":50}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BranchProductResponse.class)
                .value(r -> {
                    assert r.productId().equals(10L);
                    assert r.stock().equals(50);
                });

        // Add same product to second branch with different stock
        webTestClient.post()
                .uri("/api/v1/branches/2/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Laptop\",\"stock\":30}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BranchProductResponse.class)
                .value(r -> {
                    assert r.productId().equals(10L);
                    assert r.stock().equals(30);
                });
    }

    @Test
    @DisplayName("DELETE /api/v1/branches/{branchId}/products/{productId} should return 204 when product removed")
    void shouldReturn204WhenProductRemovedFromBranch() {
        Long branchId = 1L;
        Long productId = 10L;

        when(removeProductFromBranchUseCase.execute(eq(branchId), eq(productId)))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/branches/1/products/10")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("DELETE /api/v1/branches/{branchId}/products/{productId} should propagate error when branch not found")
    void shouldPropagateErrorWhenBranchNotFoundForRemoveProduct() {
        when(removeProductFromBranchUseCase.execute(eq(99L), eq(10L)))
                .thenReturn(Mono.error(new BusinessException(DomainErrorCode.BRANCH_NOT_FOUND)));

        webTestClient.delete()
                .uri("/api/v1/branches/99/products/10")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("DELETE /api/v1/branches/{branchId}/products/{productId} should propagate error when product not associated")
    void shouldPropagateErrorWhenProductNotAssociatedForRemove() {
        when(removeProductFromBranchUseCase.execute(eq(1L), eq(99L)))
                .thenReturn(Mono.error(new BusinessException(DomainErrorCode.BRANCH_PRODUCT_NOT_FOUND)));

        webTestClient.delete()
                .uri("/api/v1/branches/1/products/99")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
