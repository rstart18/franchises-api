package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.dto.BranchRequest;
import co.com.bancolombia.api.dto.BranchResponse;
import co.com.bancolombia.api.mapper.BranchMapper;
import co.com.bancolombia.model.branch.Branch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BranchMapperTest {

    private BranchMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BranchMapperImpl();
    }

    @Test
    @DisplayName("toDomain should map name from request and ignore id")
    void shouldMapRequestToDomain() {
        BranchRequest request = BranchRequest.builder()
                .name("North Branch")
                .build();

        Branch branch = mapper.toDomain(request);

        assertEquals("North Branch", branch.name());
        assertNull(branch.id());
    }

    @Test
    @DisplayName("toResponse should map id and name from domain")
    void shouldMapDomainToResponse() {
        Branch branch = new Branch(10L, "North Branch");

        BranchResponse response = mapper.toResponse(branch);

        assertEquals(10L, response.id());
        assertEquals("North Branch", response.name());
    }
}
