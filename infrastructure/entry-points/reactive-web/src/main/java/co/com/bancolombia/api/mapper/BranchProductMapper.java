package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.BranchProductResponse;
import co.com.bancolombia.api.dto.TopStockProductResponse;
import co.com.bancolombia.model.branchproduct.BranchProduct;
import co.com.bancolombia.model.branchproduct.TopStockProduct;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BranchProductMapper {
    BranchProductResponse toResponse(BranchProduct branchProduct);

    TopStockProductResponse toResponse(TopStockProduct topStockProduct);
}
