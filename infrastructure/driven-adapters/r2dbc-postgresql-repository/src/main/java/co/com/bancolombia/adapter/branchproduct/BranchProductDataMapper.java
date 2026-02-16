package co.com.bancolombia.adapter.branchproduct;

import co.com.bancolombia.model.branchproduct.BranchProduct;
import co.com.bancolombia.model.branchproduct.TopStockProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BranchProductDataMapper {

    @Mapping(source = "productName", target = "productName")
    BranchProduct toDomain(BranchProductData data, String productName);

    TopStockProduct toDomain(TopStockProductProjection projection);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    BranchProductData toData(BranchProduct branchProduct);
}
