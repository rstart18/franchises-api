package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.config.RequestValidator;
import co.com.bancolombia.api.dto.BranchResponse;
import co.com.bancolombia.api.dto.FranchiseResponse;
import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.usecase.franchise.AddBranchToFranchiseUseCase;
import co.com.bancolombia.usecase.franchise.CreateFranchiseUseCase;
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

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        RequestValidator requestValidator = new RequestValidator(validator);
        FranchiseHandler franchiseHandler = new FranchiseHandler(
                createFranchiseUseCase, franchiseMapper, requestValidator,
                addBranchToFranchiseUseCase, branchMapper);
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
}
