package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.BranchProductResponse;
import co.com.bancolombia.model.branchproduct.BranchProduct;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BranchProductMapper {
    BranchProductResponse toResponse(BranchProduct branchProduct);
}
