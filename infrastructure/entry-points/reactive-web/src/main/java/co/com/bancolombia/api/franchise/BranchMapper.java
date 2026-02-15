package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.dto.BranchRequest;
import co.com.bancolombia.api.dto.BranchResponse;
import co.com.bancolombia.model.branch.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BranchMapper {

    @Mapping(target = "id", ignore = true)
    Branch toDomain(BranchRequest request);

    BranchResponse toResponse(Branch branch);
}
