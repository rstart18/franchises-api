package co.com.bancolombia.api;

import co.com.bancolombia.api.config.HandlerLogger;
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
import co.com.bancolombia.model.usecase.AddBranchToFranchisePort;
import co.com.bancolombia.model.usecase.AddProductToBranchPort;
import co.com.bancolombia.model.usecase.CreateFranchisePort;
import co.com.bancolombia.model.usecase.GetTopStockProductsPort;
import co.com.bancolombia.model.usecase.RemoveProductFromBranchPort;
import co.com.bancolombia.model.usecase.UpdateBranchNamePort;
import co.com.bancolombia.model.usecase.UpdateFranchiseNamePort;
import co.com.bancolombia.model.usecase.UpdateProductNamePort;
import co.com.bancolombia.model.usecase.UpdateProductStockPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private static final String FRANCHISE_ID = "franchiseId";
    private static final String BRANCH_ID = "branchId";
    private static final String PRODUCT_ID = "productId";

    private static final String OP_CREATE_FRANCHISE = "createFranchise";
    private static final String OP_ADD_BRANCH = "addBranch";
    private static final String OP_ADD_PRODUCT = "addProduct";
    private static final String OP_REMOVE_PRODUCT = "removeProduct";
    private static final String OP_UPDATE_STOCK = "updateStock";
    private static final String OP_UPDATE_FRANCHISE_NAME = "updateFranchiseName";
    private static final String OP_UPDATE_BRANCH_NAME = "updateBranchName";
    private static final String OP_UPDATE_PRODUCT_NAME = "updateProductName";
    private static final String OP_GET_TOP_STOCK = "getTopStockProducts";

    private final CreateFranchisePort createFranchiseUseCase;
    private final FranchiseMapper franchiseMapper;
    private final RequestValidator validator;
    private final AddBranchToFranchisePort addBranchToFranchiseUseCase;
    private final BranchMapper branchMapper;
    private final AddProductToBranchPort addProductToBranchUseCase;
    private final BranchProductMapper branchProductMapper;
    private final RemoveProductFromBranchPort removeProductFromBranchUseCase;
    private final UpdateProductStockPort updateProductStockUseCase;
    private final GetTopStockProductsPort getTopStockProductsUseCase;
    private final UpdateFranchiseNamePort updateFranchiseNameUseCase;
    private final UpdateBranchNamePort updateBranchNameUseCase;
    private final UpdateProductNamePort updateProductNameUseCase;
    private final ProductMapper productMapper;
    private final HandlerLogger handlerLogger;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return request.bodyToMono(FranchiseRequest.class)
                .doOnNext(req -> handlerLogger.logRequest(OP_CREATE_FRANCHISE, req))
                .flatMap(validator::validate)
                .map(franchiseMapper::toDomain)
                .flatMap(createFranchiseUseCase::execute)
                .map(franchiseMapper::toResponse)
                .doOnNext(response -> handlerLogger.logResponse(OP_CREATE_FRANCHISE, response))
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response));
    }

    public Mono<ServerResponse> addBranch(ServerRequest request) {
        Long franchiseId = Long.valueOf(request.pathVariable(FRANCHISE_ID));
        return request.bodyToMono(BranchRequest.class)
                .doOnNext(req -> handlerLogger.logRequest(OP_ADD_BRANCH, req))
                .flatMap(validator::validate)
                .map(branchMapper::toDomain)
                .flatMap(branch -> addBranchToFranchiseUseCase.execute(franchiseId, branch))
                .map(branchMapper::toResponse)
                .doOnNext(response -> handlerLogger.logResponse(OP_ADD_BRANCH, response))
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response));
    }

    public Mono<ServerResponse> addProduct(ServerRequest request) {
        Long branchId = Long.valueOf(request.pathVariable(BRANCH_ID));
        return request.bodyToMono(AddProductToBranchRequest.class)
                .doOnNext(req -> handlerLogger.logRequest(OP_ADD_PRODUCT, req))
                .flatMap(validator::validate)
                .flatMap(req -> addProductToBranchUseCase.execute(branchId, req.getName(), req.getStock()))
                .map(branchProductMapper::toResponse)
                .doOnNext(response -> handlerLogger.logResponse(OP_ADD_PRODUCT, response))
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response));
    }

    public Mono<ServerResponse> removeProduct(ServerRequest request) {
        Long branchId = Long.valueOf(request.pathVariable(BRANCH_ID));
        Long productId = Long.valueOf(request.pathVariable(PRODUCT_ID));
        handlerLogger.logRequest(OP_REMOVE_PRODUCT, BRANCH_ID + "=" + branchId + ", " + PRODUCT_ID + "=" + productId);
        return removeProductFromBranchUseCase.execute(branchId, productId)
                .then(ServerResponse.noContent().build())
                .doOnSuccess(v -> handlerLogger.logResponse(OP_REMOVE_PRODUCT, "204 No Content"));
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        Long branchId = Long.valueOf(request.pathVariable(BRANCH_ID));
        Long productId = Long.valueOf(request.pathVariable(PRODUCT_ID));
        return request.bodyToMono(UpdateStockRequest.class)
                .doOnNext(req -> handlerLogger.logRequest(OP_UPDATE_STOCK, req))
                .flatMap(validator::validate)
                .flatMap(req -> updateProductStockUseCase.execute(branchId, productId, req.getStock()))
                .map(branchProductMapper::toResponse)
                .doOnNext(response -> handlerLogger.logResponse(OP_UPDATE_STOCK, response))
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> updateFranchiseName(ServerRequest request) {
        Long franchiseId = Long.valueOf(request.pathVariable(FRANCHISE_ID));
        return request.bodyToMono(UpdateFranchiseNameRequest.class)
                .doOnNext(req -> handlerLogger.logRequest(OP_UPDATE_FRANCHISE_NAME, req))
                .flatMap(validator::validate)
                .flatMap(req -> updateFranchiseNameUseCase.execute(franchiseId, req.getName()))
                .map(franchiseMapper::toResponse)
                .doOnNext(response -> handlerLogger.logResponse(OP_UPDATE_FRANCHISE_NAME, response))
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> updateBranchName(ServerRequest request) {
        Long branchId = Long.valueOf(request.pathVariable(BRANCH_ID));
        return request.bodyToMono(UpdateBranchNameRequest.class)
                .doOnNext(req -> handlerLogger.logRequest(OP_UPDATE_BRANCH_NAME, req))
                .flatMap(validator::validate)
                .flatMap(req -> updateBranchNameUseCase.execute(branchId, req.getName()))
                .map(branchMapper::toResponse)
                .doOnNext(response -> handlerLogger.logResponse(OP_UPDATE_BRANCH_NAME, response))
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> updateProductName(ServerRequest request) {
        Long productId = Long.valueOf(request.pathVariable(PRODUCT_ID));
        return request.bodyToMono(UpdateProductNameRequest.class)
                .doOnNext(req -> handlerLogger.logRequest(OP_UPDATE_PRODUCT_NAME, req))
                .flatMap(validator::validate)
                .flatMap(req -> updateProductNameUseCase.execute(productId, req.getName()))
                .map(productMapper::toResponse)
                .doOnNext(response -> handlerLogger.logResponse(OP_UPDATE_PRODUCT_NAME, response))
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getTopStockProducts(ServerRequest request) {
        Long franchiseId = Long.valueOf(request.pathVariable(FRANCHISE_ID));
        handlerLogger.logRequest(OP_GET_TOP_STOCK, FRANCHISE_ID + "=" + franchiseId);
        return getTopStockProductsUseCase.execute(franchiseId)
                .map(branchProductMapper::toResponse)
                .collectList()
                .doOnNext(list -> handlerLogger.logResponse(OP_GET_TOP_STOCK, list.size() + " items"))
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }
}
