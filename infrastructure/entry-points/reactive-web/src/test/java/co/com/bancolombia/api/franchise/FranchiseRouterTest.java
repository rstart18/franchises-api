package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.FranchiseHandler;
import co.com.bancolombia.api.FranchiseRouter;
import co.com.bancolombia.api.config.RequestValidator;
import co.com.bancolombia.api.dto.BranchProductResponse;
import co.com.bancolombia.api.dto.BranchResponse;
import co.com.bancolombia.api.dto.FranchiseResponse;
import co.com.bancolombia.api.dto.ProductResponse;
import co.com.bancolombia.api.dto.TopStockProductResponse;
import co.com.bancolombia.api.mapper.BranchMapper;
import co.com.bancolombia.api.mapper.BranchProductMapper;
import co.com.bancolombia.api.mapper.FranchiseMapper;
import co.com.bancolombia.api.mapper.ProductMapper;
import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branchproduct.BranchProduct;
import co.com.bancolombia.model.branchproduct.TopStockProduct;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.product.Product;
import co.com.bancolombia.usecase.franchise.AddBranchToFranchiseUseCase;
import co.com.bancolombia.usecase.franchise.CreateFranchiseUseCase;
import co.com.bancolombia.usecase.franchise.UpdateFranchiseNameUseCase;
import co.com.bancolombia.usecase.branch.UpdateBranchNameUseCase;
import co.com.bancolombia.usecase.product.AddProductToBranchUseCase;
import co.com.bancolombia.usecase.product.GetTopStockProductsUseCase;
import co.com.bancolombia.usecase.product.RemoveProductFromBranchUseCase;
import co.com.bancolombia.usecase.product.UpdateProductNameUseCase;
import co.com.bancolombia.usecase.product.UpdateProductStockUseCase;
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
import reactor.core.publisher.Flux;
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

    @Mock
    private UpdateProductStockUseCase updateProductStockUseCase;

    @Mock
    private GetTopStockProductsUseCase getTopStockProductsUseCase;

    @Mock
    private UpdateFranchiseNameUseCase updateFranchiseNameUseCase;

    @Mock
    private UpdateBranchNameUseCase updateBranchNameUseCase;

    @Mock
    private UpdateProductNameUseCase updateProductNameUseCase;

    @Mock
    private ProductMapper productMapper;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        RequestValidator requestValidator = new RequestValidator(validator);
        FranchiseHandler franchiseHandler = new FranchiseHandler(
                createFranchiseUseCase, franchiseMapper, requestValidator,
                addBranchToFranchiseUseCase, branchMapper, addProductToBranchUseCase,
                branchProductMapper, removeProductFromBranchUseCase, updateProductStockUseCase,
                getTopStockProductsUseCase, updateFranchiseNameUseCase, updateBranchNameUseCase,
                updateProductNameUseCase, productMapper);
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

    @Test
    @DisplayName("PATCH /api/v1/branches/{branchId}/products/{productId}/stock should return 200 with updated stock")
    void shouldReturn200WhenStockUpdated() {
        Long branchId = 1L;
        Long productId = 10L;
        Integer newStock = 75;
        BranchProduct updatedBp = new BranchProduct(productId, branchId, "Laptop", newStock);
        BranchProductResponse response = new BranchProductResponse(productId, branchId, "Laptop", newStock);

        when(updateProductStockUseCase.execute(eq(branchId), eq(productId), eq(newStock)))
                .thenReturn(Mono.just(updatedBp));
        when(branchProductMapper.toResponse(updatedBp)).thenReturn(response);

        webTestClient.patch()
                .uri("/api/v1/branches/1/products/10/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":75}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BranchProductResponse.class)
                .value(r -> {
                    assert r.productId().equals(productId);
                    assert r.branchId().equals(branchId);
                    assert r.productName().equals("Laptop");
                    assert r.stock().equals(newStock);
                });
    }

    @Test
    @DisplayName("PATCH /api/v1/branches/{branchId}/products/{productId}/stock should propagate error when branch not found")
    void shouldPropagateErrorWhenBranchNotFoundForUpdateStock() {
        when(updateProductStockUseCase.execute(eq(99L), eq(10L), eq(75)))
                .thenReturn(Mono.error(new BusinessException(DomainErrorCode.BRANCH_NOT_FOUND)));

        webTestClient.patch()
                .uri("/api/v1/branches/99/products/10/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":75}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("PATCH /api/v1/branches/{branchId}/products/{productId}/stock should propagate error when product not associated")
    void shouldPropagateErrorWhenProductNotAssociatedForUpdateStock() {
        when(updateProductStockUseCase.execute(eq(1L), eq(99L), eq(75)))
                .thenReturn(Mono.error(new BusinessException(DomainErrorCode.BRANCH_PRODUCT_NOT_FOUND)));

        webTestClient.patch()
                .uri("/api/v1/branches/1/products/99/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":75}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("PATCH /api/v1/branches/{branchId}/products/{productId}/stock should return 500 when stock is negative")
    void shouldReturn500WhenUpdateStockIsNegative() {
        webTestClient.patch()
                .uri("/api/v1/branches/1/products/10/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":-5}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("PATCH /api/v1/branches/{branchId}/products/{productId}/stock should return 500 when stock is missing")
    void shouldReturn500WhenUpdateStockIsMissing() {
        webTestClient.patch()
                .uri("/api/v1/branches/1/products/10/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("GET /api/v1/franchises/{franchiseId}/products/top-stock should return 200 with top stock products")
    void shouldReturn200WithTopStockProducts() {
        TopStockProduct tp1 = new TopStockProduct(100L, "Laptop", 150, 10L, "North Branch");
        TopStockProduct tp2 = new TopStockProduct(200L, "Phone", 80, 20L, "South Branch");
        TopStockProductResponse resp1 = new TopStockProductResponse(100L, "Laptop", 150, 10L, "North Branch");
        TopStockProductResponse resp2 = new TopStockProductResponse(200L, "Phone", 80, 20L, "South Branch");

        when(getTopStockProductsUseCase.execute(eq(1L))).thenReturn(Flux.just(tp1, tp2));
        when(branchProductMapper.toResponse(tp1)).thenReturn(resp1);
        when(branchProductMapper.toResponse(tp2)).thenReturn(resp2);

        webTestClient.get()
                .uri("/api/v1/franchises/1/products/top-stock")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TopStockProductResponse.class)
                .hasSize(2)
                .value(list -> {
                    assert list.get(0).productName().equals("Laptop");
                    assert list.get(0).stock().equals(150);
                    assert list.get(0).branchName().equals("North Branch");
                    assert list.get(1).productName().equals("Phone");
                    assert list.get(1).stock().equals(80);
                    assert list.get(1).branchName().equals("South Branch");
                });
    }

    @Test
    @DisplayName("GET /api/v1/franchises/{franchiseId}/products/top-stock should return 200 with empty list")
    void shouldReturn200WithEmptyListWhenNoProducts() {
        when(getTopStockProductsUseCase.execute(eq(1L))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/franchises/1/products/top-stock")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TopStockProductResponse.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("GET /api/v1/franchises/{franchiseId}/products/top-stock should propagate error when franchise not found")
    void shouldPropagateErrorWhenFranchiseNotFoundForTopStock() {
        when(getTopStockProductsUseCase.execute(eq(999L)))
                .thenReturn(Flux.error(new BusinessException(DomainErrorCode.FRANCHISE_NOT_FOUND)));

        webTestClient.get()
                .uri("/api/v1/franchises/999/products/top-stock")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("PATCH /api/v1/franchises/{franchiseId} should return 200 with updated franchise")
    void shouldReturn200WhenFranchiseNameUpdated() {
        Long franchiseId = 1L;
        String newName = "New Burger Kingdom";
        Franchise updated = Franchise.builder().id(franchiseId).name(newName).build();
        FranchiseResponse response = new FranchiseResponse(franchiseId, newName);

        when(updateFranchiseNameUseCase.execute(eq(franchiseId), eq(newName)))
                .thenReturn(Mono.just(updated));
        when(franchiseMapper.toResponse(updated)).thenReturn(response);

        webTestClient.patch()
                .uri("/api/v1/franchises/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"New Burger Kingdom\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(FranchiseResponse.class)
                .value(r -> {
                    assert r.id().equals(franchiseId);
                    assert r.name().equals(newName);
                });
    }

    @Test
    @DisplayName("PATCH /api/v1/franchises/{franchiseId} should propagate error when franchise not found")
    void shouldPropagateErrorWhenFranchiseNotFoundForUpdateName() {
        when(updateFranchiseNameUseCase.execute(eq(999L), eq("New Name")))
                .thenReturn(Mono.error(new BusinessException(DomainErrorCode.FRANCHISE_NOT_FOUND)));

        webTestClient.patch()
                .uri("/api/v1/franchises/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"New Name\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("PATCH /api/v1/franchises/{franchiseId} should propagate error when name already exists")
    void shouldPropagateErrorWhenNameAlreadyExistsForUpdateName() {
        when(updateFranchiseNameUseCase.execute(eq(1L), eq("Existing Name")))
                .thenReturn(Mono.error(new BusinessException(DomainErrorCode.FRANCHISE_NAME_ALREADY_EXISTS)));

        webTestClient.patch()
                .uri("/api/v1/franchises/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Existing Name\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("PATCH /api/v1/franchises/{franchiseId} should return 500 when name is blank")
    void shouldReturn500WhenFranchiseNameIsBlankForUpdate() {
        webTestClient.patch()
                .uri("/api/v1/franchises/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("PATCH /api/v1/branches/{branchId} should return 200 with updated branch")
    void shouldReturn200WhenBranchNameUpdated() {
        Long branchId = 10L;
        String newName = "New North Branch";
        Branch updated = new Branch(branchId, newName);
        BranchResponse response = new BranchResponse(branchId, newName);

        when(updateBranchNameUseCase.execute(eq(branchId), eq(newName)))
                .thenReturn(Mono.just(updated));
        when(branchMapper.toResponse(updated)).thenReturn(response);

        webTestClient.patch()
                .uri("/api/v1/branches/10")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"New North Branch\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BranchResponse.class)
                .value(r -> {
                    assert r.id().equals(branchId);
                    assert r.name().equals(newName);
                });
    }

    @Test
    @DisplayName("PATCH /api/v1/branches/{branchId} should propagate error when branch not found")
    void shouldPropagateErrorWhenBranchNotFoundForUpdateName() {
        when(updateBranchNameUseCase.execute(eq(999L), eq("New Name")))
                .thenReturn(Mono.error(new BusinessException(DomainErrorCode.BRANCH_NOT_FOUND)));

        webTestClient.patch()
                .uri("/api/v1/branches/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"New Name\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("PATCH /api/v1/branches/{branchId} should propagate error when name already exists")
    void shouldPropagateErrorWhenBranchNameAlreadyExists() {
        when(updateBranchNameUseCase.execute(eq(10L), eq("Existing Branch")))
                .thenReturn(Mono.error(new BusinessException(DomainErrorCode.BRANCH_NAME_ALREADY_EXISTS)));

        webTestClient.patch()
                .uri("/api/v1/branches/10")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Existing Branch\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("PATCH /api/v1/branches/{branchId} should return 500 when name is blank")
    void shouldReturn500WhenBranchNameIsBlankForUpdate() {
        webTestClient.patch()
                .uri("/api/v1/branches/10")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{productId} should return 200 with updated product")
    void shouldReturn200WhenProductNameUpdated() {
        Long productId = 10L;
        String newName = "New Laptop Pro";
        Product updated = new Product(productId, newName);
        ProductResponse response = new ProductResponse(productId, newName);

        when(updateProductNameUseCase.execute(eq(productId), eq(newName)))
                .thenReturn(Mono.just(updated));
        when(productMapper.toResponse(updated)).thenReturn(response);

        webTestClient.patch()
                .uri("/api/v1/products/10")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"New Laptop Pro\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse.class)
                .value(r -> {
                    assert r.id().equals(productId);
                    assert r.name().equals(newName);
                });
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{productId} should propagate error when product not found")
    void shouldPropagateErrorWhenProductNotFoundForUpdateName() {
        when(updateProductNameUseCase.execute(eq(999L), eq("New Name")))
                .thenReturn(Mono.error(new BusinessException(DomainErrorCode.PRODUCT_NOT_FOUND)));

        webTestClient.patch()
                .uri("/api/v1/products/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"New Name\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{productId} should propagate error when name already exists")
    void shouldPropagateErrorWhenProductNameAlreadyExists() {
        when(updateProductNameUseCase.execute(eq(10L), eq("Existing Product")))
                .thenReturn(Mono.error(new BusinessException(DomainErrorCode.PRODUCT_NAME_ALREADY_EXISTS)));

        webTestClient.patch()
                .uri("/api/v1/products/10")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Existing Product\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{productId} should return 500 when name is blank")
    void shouldReturn500WhenProductNameIsBlankForUpdate() {
        webTestClient.patch()
                .uri("/api/v1/products/10")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
