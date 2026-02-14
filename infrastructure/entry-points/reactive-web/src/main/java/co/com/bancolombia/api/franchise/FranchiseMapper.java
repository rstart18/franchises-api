package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.dto.FranchiseRequest;
import co.com.bancolombia.api.dto.FranchiseResponse;
import co.com.bancolombia.model.franchise.Franchise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FranchiseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "branches", ignore = true)
    Franchise toDomain(FranchiseRequest request);

    FranchiseResponse toResponse(Franchise franchise);
}
