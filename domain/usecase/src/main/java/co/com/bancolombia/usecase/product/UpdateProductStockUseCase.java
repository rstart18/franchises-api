package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.branch.gateway.BranchRepository;
import co.com.bancolombia.model.branchproduct.BranchProduct;
import co.com.bancolombia.model.branchproduct.StockAlert;
import co.com.bancolombia.model.branchproduct.gateway.BranchProductRepository;
import co.com.bancolombia.model.branchproduct.gateway.StockAlertGateway;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateProductStockUseCase {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final BranchRepository branchRepository;
    private final BranchProductRepository branchProductRepository;
    private final StockAlertGateway stockAlertGateway;

    public Mono<BranchProduct> execute(Long branchId, Long productId, Integer stock) {
        return Mono.justOrEmpty(stock)
                .filter(s -> s >= 0)
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.PRODUCT_STOCK_INVALID)))
                .flatMap(s -> branchRepository.findById(branchId))
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BRANCH_NOT_FOUND)))
                .flatMap(branch -> branchProductRepository.findActiveByBranchAndProduct(branchId, productId))
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BRANCH_PRODUCT_NOT_FOUND)))
                .flatMap(bp -> branchProductRepository.updateStock(branchId, productId, stock))
                .flatMap(this::sendAlertIfLowStock);
    }

    private boolean isLowStock(BranchProduct product) {
        return product.stock() < LOW_STOCK_THRESHOLD;
    }

    private Mono<BranchProduct> sendAlertIfLowStock(BranchProduct updatedProduct) {
        return Mono.just(updatedProduct)
                .filter(this::isLowStock)
                .flatMap(this::sendAlert)
                .switchIfEmpty(Mono.just(updatedProduct));
    }

    private Mono<BranchProduct> sendAlert(BranchProduct product) {
        return stockAlertGateway.sendAlert(StockAlert.from(product))
                .onErrorResume(error -> Mono.empty())
                .thenReturn(product);
    }
}