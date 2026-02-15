package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.config.RequestValidator;
import co.com.bancolombia.api.dto.AddProductToBranchRequest;
import co.com.bancolombia.api.dto.BranchRequest;
import co.com.bancolombia.api.dto.FranchiseRequest;
import co.com.bancolombia.api.dto.UpdateBranchNameRequest;
import co.com.bancolombia.api.dto.UpdateFranchiseNameRequest;
import co.com.bancolombia.api.dto.UpdateProductNameRequest;
import co.com.bancolombia.api.dto.UpdateStockRequest;
import co.com.bancolombia.api.mapper.BranchMapper;
import co.com.bancolombia.api.mapper.BranchProductMapper;
import co.com.bancolombia.api.mapper.FranchiseMapper;
import co.com.bancolombia.api.mapper.ProductMapper;
import co.com.bancolombia.usecase.franchise.AddBranchToFranchiseUseCase;
import co.com.bancolombia.usecase.franchise.CreateFranchiseUseCase;
import co.com.bancolombia.usecase.franchise.UpdateFranchiseNameUseCase;
import co.com.bancolombia.usecase.branch.UpdateBranchNameUseCase;
import co.com.bancolombia.usecase.product.AddProductToBranchUseCase;
import co.com.bancolombia.usecase.product.GetTopStockProductsUseCase;
import co.com.bancolombia.usecase.product.RemoveProductFromBranchUseCase;
import co.com.bancolombia.usecase.product.UpdateProductNameUseCase;
import co.com.bancolombia.usecase.product.UpdateProductStockUseCase;
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
    private final RemoveProductFromBranchUseCase removeProductFromBranchUseCase;
    private final UpdateProductStockUseCase updateProductStockUseCase;
    private final GetTopStockProductsUseCase getTopStockProductsUseCase;
    private final UpdateFranchiseNameUseCase updateFranchiseNameUseCase;
    private final UpdateBranchNameUseCase updateBranchNameUseCase;
    private final UpdateProductNameUseCase updateProductNameUseCase;
    private final ProductMapper productMapper;

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

    public Mono<ServerResponse> removeProduct(ServerRequest request) {
        Long branchId = Long.valueOf(request.pathVariable("branchId"));
        Long productId = Long.valueOf(request.pathVariable("productId"));
        return removeProductFromBranchUseCase.execute(branchId, productId)
                .then(ServerResponse.noContent().build())
                .doOnSuccess(v -> log.info("Product {} removed from branch {} successfully", productId, branchId))
                .doOnError(e -> log.error("Error removing product {} from branch {}: {}", productId, branchId, e.getMessage()));
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        Long branchId = Long.valueOf(request.pathVariable("branchId"));
        Long productId = Long.valueOf(request.pathVariable("productId"));
        return request.bodyToMono(UpdateStockRequest.class)
                .flatMap(validator::validate)
                .flatMap(req -> updateProductStockUseCase.execute(branchId, productId, req.getStock()))
                .map(branchProductMapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnSuccess(v -> log.info("Stock updated for product {} in branch {} successfully", productId, branchId))
                .doOnError(e -> log.error("Error updating stock for product {} in branch {}: {}", productId, branchId, e.getMessage()));
    }

    public Mono<ServerResponse> updateFranchiseName(ServerRequest request) {
        Long franchiseId = Long.valueOf(request.pathVariable("franchiseId"));
        return request.bodyToMono(UpdateFranchiseNameRequest.class)
                .flatMap(validator::validate)
                .flatMap(req -> updateFranchiseNameUseCase.execute(franchiseId, req.getName()))
                .map(franchiseMapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnSuccess(v -> log.info("Franchise {} name updated successfully", franchiseId))
                .doOnError(e -> log.error("Error updating franchise {} name: {}", franchiseId, e.getMessage()));
    }

    public Mono<ServerResponse> updateBranchName(ServerRequest request) {
        Long branchId = Long.valueOf(request.pathVariable("branchId"));
        return request.bodyToMono(UpdateBranchNameRequest.class)
                .flatMap(validator::validate)
                .flatMap(req -> updateBranchNameUseCase.execute(branchId, req.getName()))
                .map(branchMapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnSuccess(v -> log.info("Branch {} name updated successfully", branchId))
                .doOnError(e -> log.error("Error updating branch {} name: {}", branchId, e.getMessage()));
    }

    public Mono<ServerResponse> updateProductName(ServerRequest request) {
        Long productId = Long.valueOf(request.pathVariable("productId"));
        return request.bodyToMono(UpdateProductNameRequest.class)
                .flatMap(validator::validate)
                .flatMap(req -> updateProductNameUseCase.execute(productId, req.getName()))
                .map(productMapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnSuccess(v -> log.info("Product {} name updated successfully", productId))
                .doOnError(e -> log.error("Error updating product {} name: {}", productId, e.getMessage()));
    }

    public Mono<ServerResponse> getTopStockProducts(ServerRequest request) {
        Long franchiseId = Long.valueOf(request.pathVariable("franchiseId"));
        return getTopStockProductsUseCase.execute(franchiseId)
                .map(branchProductMapper::toResponse)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list))
                .doOnSuccess(v -> log.info("Top stock products retrieved for franchise {}", franchiseId))
                .doOnError(e -> log.error("Error getting top stock products for franchise {}: {}", franchiseId, e.getMessage()));
    }
}
