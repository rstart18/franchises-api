package co.com.bancolombia.usecase.franchise;

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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFranchiseNameUseCaseTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    private UpdateFranchiseNameUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateFranchiseNameUseCase(franchiseRepository);
    }

    @Test
    @DisplayName("Should update franchise name successfully when name is valid, franchise exists and name is unique")
    void shouldUpdateFranchiseNameSuccessfully() {
        Long franchiseId = 1L;
        String newName = "New Burger Kingdom";
        Franchise existing = Franchise.builder().id(franchiseId).name("Burger Kingdom").build();
        Franchise updated = Franchise.builder().id(franchiseId).name(newName).build();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(existing));
        when(franchiseRepository.existsByName(newName)).thenReturn(Mono.just(false));
        when(franchiseRepository.updateName(franchiseId, newName)).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.execute(franchiseId, newName))
                .expectNextMatches(result ->
                        result.getId().equals(franchiseId) &&
                        result.getName().equals(newName))
                .verifyComplete();

        verify(franchiseRepository).updateName(franchiseId, newName);
    }

    @Test
    @DisplayName("Should throw FRANCHISE_NAME_REQUIRED when name is null")
    void shouldThrowWhenNameIsNull() {
        StepVerifier.create(useCase.execute(1L, null))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.FRANCHISE_NAME_REQUIRED)
                .verify();

        verify(franchiseRepository, never()).findById(anyLong());
        verify(franchiseRepository, never()).updateName(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should throw FRANCHISE_NAME_REQUIRED when name is blank")
    void shouldThrowWhenNameIsBlank() {
        StepVerifier.create(useCase.execute(1L, "   "))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.FRANCHISE_NAME_REQUIRED)
                .verify();

        verify(franchiseRepository, never()).findById(anyLong());
        verify(franchiseRepository, never()).updateName(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should throw FRANCHISE_NOT_FOUND when franchise does not exist")
    void shouldThrowWhenFranchiseNotFound() {
        Long franchiseId = 999L;
        String newName = "New Name";

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(franchiseId, newName))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.FRANCHISE_NOT_FOUND)
                .verify();

        verify(franchiseRepository, never()).existsByName(anyString());
        verify(franchiseRepository, never()).updateName(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should throw FRANCHISE_NAME_ALREADY_EXISTS when name is already taken")
    void shouldThrowWhenNameAlreadyExists() {
        Long franchiseId = 1L;
        String newName = "Existing Name";
        Franchise existing = Franchise.builder().id(franchiseId).name("Burger Kingdom").build();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(existing));
        when(franchiseRepository.existsByName(newName)).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(franchiseId, newName))
                .expectErrorMatches(e ->
                        e instanceof BusinessException &&
                        ((BusinessException) e).getErrorCode() == DomainErrorCode.FRANCHISE_NAME_ALREADY_EXISTS)
                .verify();

        verify(franchiseRepository, never()).updateName(anyLong(), anyString());
    }
}
