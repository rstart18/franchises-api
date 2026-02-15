package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.config.RequestValidator;
import co.com.bancolombia.api.dto.AddProductToBranchRequest;
import co.com.bancolombia.api.dto.BranchProductResponse;
import co.com.bancolombia.api.dto.BranchRequest;
import co.com.bancolombia.api.dto.FranchiseRequest;
import co.com.bancolombia.usecase.franchise.AddBranchToFranchiseUseCase;
import co.com.bancolombia.usecase.franchise.CreateFranchiseUseCase;
import co.com.bancolombia.usecase.product.AddProductToBranchUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class FranchiseHandler {

    private final CreateFranchiseUseCase createFranchiseUseCase;
    private final FranchiseMapper franchiseMapper;
    private final RequestValidator validator;
    private final AddBranchToFranchiseUseCase addBranchToFranchiseUseCase;
    private final BranchMapper branchMapper;
    private final AddProductToBranchUseCase addProductToBranchUseCase;
    private final BranchProductMapper branchProductMapper;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return request.bodyToMono(FranchiseRequest.class)
                .flatMap(validator::validate)
                .map(franchiseMapper::toDomain)
                .flatMap(createFranchiseUseCase::execute)
                .map(franchiseMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response))
                .doOnSuccess(v -> log.info("Franchise created successfully"))
                .doOnError(e -> log.error("Error creating franchise: {}", e.getMessage()));
    }

    public Mono<ServerResponse> addBranch(ServerRequest request) {
        Long franchiseId = Long.valueOf(request.pathVariable("franchiseId"));
        return request.bodyToMono(BranchRequest.class)
                .flatMap(validator::validate)
                .map(branchMapper::toDomain)
                .flatMap(branch -> addBranchToFranchiseUseCase.execute(franchiseId, branch))
                .map(branchMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response))
                .doOnSuccess(v -> log.info("Branch added to franchise {} successfully", franchiseId))
                .doOnError(e -> log.error("Error adding branch to franchise {}: {}", franchiseId, e.getMessage()));
    }

    public Mono<ServerResponse> addProduct(ServerRequest request) {
        Long branchId = Long.valueOf(request.pathVariable("branchId"));
        return request.bodyToMono(AddProductToBranchRequest.class)
                .flatMap(validator::validate)
                .flatMap(req -> addProductToBranchUseCase.execute(branchId, req.getName(), req.getStock()))
                .map(branchProductMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response))
                .doOnSuccess(v -> log.info("Product added to branch {} successfully", branchId))
                .doOnError(e -> log.error("Error adding product to branch {}: {}", branchId, e.getMessage()));
    }
}
