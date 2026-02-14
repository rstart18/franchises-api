package co.com.bancolombia.model.franchise;

import co.com.bancolombia.model.branch.Branch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FranchiseTest {

    @Test
    @DisplayName("Should build Franchise using builder")
    void shouldBuildFranchise() {
        List<Branch> branches = List.of(new Branch(1L, "North Branch"));
        Franchise franchise = Franchise.builder()
                .id(1L)
                .name("Burger Kingdom")
                .branches(branches)
                .build();

        assertEquals(1L, franchise.getId());
        assertEquals("Burger Kingdom", franchise.getName());
        assertEquals(1, franchise.getBranches().size());
    }

    @Test
    @DisplayName("Should build Franchise with no-args constructor")
    void shouldBuildFranchiseWithNoArgsConstructor() {
        Franchise franchise = new Franchise();
        assertNull(franchise.getId());
        assertNull(franchise.getName());
        assertNull(franchise.getBranches());
    }

    @Test
    @DisplayName("Should copy Franchise using toBuilder")
    void shouldCopyFranchiseWithToBuilder() {
        Franchise original = Franchise.builder().id(1L).name("Old Name").build();
        Franchise copy = original.toBuilder().name("New Name").build();

        assertEquals(1L, copy.getId());
        assertEquals("New Name", copy.getName());
    }

    @Test
    @DisplayName("Branch record should hold id and name")
    void branchRecordShouldHoldValues() {
        Branch branch = new Branch(10L, "East Branch");
        assertEquals(10L, branch.id());
        assertEquals("East Branch", branch.name());
    }
}
