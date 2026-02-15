package co.com.bancolombia.adapter.branchproduct;

import co.com.bancolombia.model.branchproduct.BranchProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BranchProductDataMapper {

    @Mapping(source = "productName", target = "productName")
    BranchProduct toDomain(BranchProductData data, String productName);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    BranchProductData toData(BranchProduct branchProduct);
}
