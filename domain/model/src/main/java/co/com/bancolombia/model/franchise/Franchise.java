package co.com.bancolombia.model.franchise;

import co.com.bancolombia.model.branch.Branch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Franchise {
    private Long id;
    private String name;
    private List<Branch> branches;
}
