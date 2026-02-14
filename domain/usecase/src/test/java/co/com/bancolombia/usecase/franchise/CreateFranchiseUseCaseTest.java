package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.franchise.gateway.FranchiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFranchiseUseCaseTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    private CreateFranchiseUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateFranchiseUseCase(franchiseRepository);
    }

    @Test
    @DisplayName("Should create franchise successfully when name is valid and unique")
    void shouldCreateFranchiseSuccessfully() {
        Franchise franchise = buildFranchise("Burger Kingdom");
        Franchise saved = franchise.toBuilder().id(1L).build();

        when(franchiseRepository.existsByName("Burger Kingdom")).thenReturn(Mono.just(false));
        when(franchiseRepository.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute(franchise))
                .expectNextMatches(result ->
                        result.getId() == 1L &&
                        result.getName().equals("Burger Kingdom"))
                .verifyComplete();

        verify(franchiseRepository).save(any(Franchise.class));
    }

    @Test
    @DisplayName("Should fail when franchise name is null")
    void shouldFailWhenNameIsNull() {
        Franchise franchise = buildFranchise(null);

        StepVerifier.create(useCase.execute(franchise))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.FRANCHISE_NAME_REQUIRED)
                .verify();

        verify(franchiseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when franchise name is blank")
    void shouldFailWhenNameIsBlank() {
        Franchise franchise = buildFranchise("   ");

        StepVerifier.create(useCase.execute(franchise))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.FRANCHISE_NAME_REQUIRED)
                .verify();

        verify(franchiseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when franchise name already exists")
    void shouldFailWhenNameAlreadyExists() {
        Franchise franchise = buildFranchise("Burger Kingdom");

        when(franchiseRepository.existsByName("Burger Kingdom")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(franchise))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.FRANCHISE_NAME_ALREADY_EXISTS)
                .verify();

        verify(franchiseRepository, never()).save(any());
        verify(franchiseRepository).existsByName(anyString());
    }

    @Test
    @DisplayName("Should create franchise with branches")
    void shouldCreateFranchiseWithBranches() {
        List<Branch> branches = List.of(new Branch(null, "North Branch"), new Branch(null, "South Branch"));
        Franchise franchise = Franchise.builder().name("Burger Kingdom").branches(branches).build();
        Franchise saved = franchise.toBuilder().id(1L).build();

        when(franchiseRepository.existsByName("Burger Kingdom")).thenReturn(Mono.just(false));
        when(franchiseRepository.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute(franchise))
                .expectNextMatches(result -> result.getName().equals("Burger Kingdom"))
                .verifyComplete();

        verify(franchiseRepository).save(any(Franchise.class));
    }

    private Franchise buildFranchise(String name) {
        return Franchise.builder().name(name).build();
    }
}
