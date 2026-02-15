package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.branch.gateway.BranchRepository;
import co.com.bancolombia.model.branchproduct.BranchProduct;
import co.com.bancolombia.model.branchproduct.gateway.BranchProductRepository;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.product.Product;
import co.com.bancolombia.model.product.gateway.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AddProductToBranchUseCase {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final BranchProductRepository branchProductRepository;

    public Mono<BranchProduct> execute(Long branchId, String productName, Integer stock) {
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BRANCH_NOT_FOUND)))
                .flatMap(branch -> {
                    // Validate inputs after confirming branch exists
                    if (productName == null || productName.isBlank()) {
                        return Mono.error(new BusinessException(DomainErrorCode.PRODUCT_NAME_REQUIRED));
                    }
                    if (stock == null || stock < 0) {
                        return Mono.error(new BusinessException(DomainErrorCode.PRODUCT_STOCK_INVALID));
                    }
                    // Find or create product
                    return productRepository.findByName(productName)
                            .switchIfEmpty(Mono.defer(() -> productRepository.save(new Product(null, productName))));
                })
                .flatMap(product ->
                        branchProductRepository.save(BranchProduct.from(product, branchId, stock))
                );
    }
}
