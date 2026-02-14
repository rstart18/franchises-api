package co.com.bancolombia.adapter.franchise;

import co.com.bancolombia.model.franchise.Franchise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseRepositoryAdapterTest {

    @Mock
    private FranchiseR2dbcRepository franchiseR2dbcRepository;

    @Mock
    private FranchiseDataMapper mapper;

    private FranchiseRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FranchiseRepositoryAdapter(franchiseR2dbcRepository, mapper);
    }

    @Test
    @DisplayName("Should save franchise and return domain object with generated id")
    void shouldSaveFranchise() {
        Franchise franchise = Franchise.builder().name("Burger Kingdom").build();
        FranchiseData franchiseData = FranchiseData.builder().name("Burger Kingdom").build();
        FranchiseData savedData = FranchiseData.builder().id(1L).name("Burger Kingdom").build();
        Franchise domainResult = Franchise.builder().id(1L).name("Burger Kingdom").build();

        when(mapper.toData(franchise)).thenReturn(franchiseData);
        when(franchiseR2dbcRepository.save(franchiseData)).thenReturn(Mono.just(savedData));
        when(mapper.toDomain(savedData)).thenReturn(domainResult);

        StepVerifier.create(adapter.save(franchise))
                .expectNextMatches(result ->
                        result.getId() == 1L &&
                        result.getName().equals("Burger Kingdom"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return true when franchise name exists")
    void shouldReturnTrueWhenFranchiseNameExists() {
        when(franchiseR2dbcRepository.existsByName("Burger Kingdom")).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsByName("Burger Kingdom"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return false when franchise name does not exist")
    void shouldReturnFalseWhenFranchiseNameDoesNotExist() {
        when(franchiseR2dbcRepository.existsByName(anyString())).thenReturn(Mono.just(false));

        StepVerifier.create(adapter.existsByName("New Franchise"))
                .expectNext(false)
                .verifyComplete();
    }
}
