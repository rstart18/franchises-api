package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.branch.gateway.BranchRepository;
import co.com.bancolombia.model.branchproduct.BranchProduct;
import co.com.bancolombia.model.branchproduct.gateway.BranchProductRepository;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.product.Product;
import co.com.bancolombia.model.product.gateway.ProductRepository;
import co.com.bancolombia.model.usecase.AddProductToBranchPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AddProductToBranchUseCase extends AddProductToBranchPort {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final BranchProductRepository branchProductRepository;

    @Override
    public Mono<BranchProduct> execute(Long branchId, String productName, Integer stock) {
        return Mono.justOrEmpty(productName)
                .filter(name -> !name.isBlank())
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.PRODUCT_NAME_REQUIRED)))
                .then(Mono.justOrEmpty(stock))
                .filter(s -> s >= 0)
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.PRODUCT_STOCK_INVALID)))
                .flatMap(s -> branchRepository.findById(branchId))
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BRANCH_NOT_FOUND)))
                .flatMap(branch -> productRepository.findByName(productName)
                        .switchIfEmpty(Mono.defer(() -> productRepository.save(new Product(null, productName)))))
                .flatMap(product -> branchProductRepository.save(BranchProduct.from(product, branchId, stock)));
    }
}
