package co.com.bancolombia.adapter.franchise;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.franchise.Franchise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FranchiseDataMapper {

    @Mapping(target = "branches", ignore = true)
    Franchise toDomain(FranchiseData data);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    FranchiseData toData(Franchise franchise);

    Branch branchToDomain(BranchData data);

    @Mapping(target = "franchiseId", source = "franchiseId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    BranchData branchToData(Branch branch, Long franchiseId);

    List<Branch> branchListToDomain(List<BranchData> dataList);
}
