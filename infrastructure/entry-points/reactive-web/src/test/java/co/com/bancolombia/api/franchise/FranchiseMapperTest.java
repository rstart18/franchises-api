package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.dto.FranchiseRequest;
import co.com.bancolombia.api.dto.FranchiseResponse;
import co.com.bancolombia.api.mapper.FranchiseMapper;
import co.com.bancolombia.model.franchise.Franchise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FranchiseMapperTest {

    private FranchiseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FranchiseMapperImpl();
    }

    @Test
    @DisplayName("toDomain should map name from request and ignore id and branches")
    void shouldMapRequestToDomain() {
        FranchiseRequest request = FranchiseRequest.builder()
                .name("Burger Kingdom")
                .build();

        Franchise domain = mapper.toDomain(request);

        assertEquals("Burger Kingdom", domain.getName());
        assertNull(domain.getId());
        assertNull(domain.getBranches());
    }

    @Test
    @DisplayName("toResponse should map id and name from domain")
    void shouldMapDomainToResponse() {
        Franchise franchise = Franchise.builder()
                .id(1L)
                .name("Burger Kingdom")
                .build();

        FranchiseResponse response = mapper.toResponse(franchise);

        assertEquals(1L, response.id());
        assertEquals("Burger Kingdom", response.name());
    }
}
