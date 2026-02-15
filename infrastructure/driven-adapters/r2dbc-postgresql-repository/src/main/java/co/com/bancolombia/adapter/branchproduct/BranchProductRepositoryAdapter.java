package co.com.bancolombia.adapter.branchproduct;

import co.com.bancolombia.adapter.product.ProductR2dbcRepository;
import co.com.bancolombia.model.branchproduct.BranchProduct;
import co.com.bancolombia.model.branchproduct.gateway.BranchProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class BranchProductRepositoryAdapter implements BranchProductRepository {

    private final BranchProductR2dbcRepository branchProductR2dbcRepository;
    private final ProductR2dbcRepository productR2dbcRepository;
    private final BranchProductDataMapper mapper;

    @Override
    public Mono<BranchProduct> save(BranchProduct branchProduct) {
        return branchProductR2dbcRepository.save(mapper.toData(branchProduct))
                .flatMap(saved -> productR2dbcRepository.findById(saved.getProductId())
                        .map(productData -> mapper.toDomain(saved, productData.getName()))
                );
    }

    @Override
    public Mono<BranchProduct> findByBranchAndProduct(Long branchId, Long productId) {
        return branchProductR2dbcRepository.findByBranchAndProduct(branchId, productId)
                .flatMap(data -> productR2dbcRepository.findById(data.getProductId())
                        .map(productData -> mapper.toDomain(data, productData.getName()))
                );
    }

    @Override
    public Mono<Void> softDelete(Long branchId, Long productId) {
        return branchProductR2dbcRepository.softDelete(branchId, productId);
    }

    @Override
    public Mono<BranchProduct> findActiveByBranchAndProduct(Long branchId, Long productId) {
        return branchProductR2dbcRepository.findActiveByBranchAndProduct(branchId, productId)
                .flatMap(data -> productR2dbcRepository.findById(data.getProductId())
                        .map(productData -> mapper.toDomain(data, productData.getName()))
                );
    }

    @Override
    public Mono<BranchProduct> updateStock(Long branchId, Long productId, Integer stock) {
        return branchProductR2dbcRepository.findActiveByBranchAndProduct(branchId, productId)
                .flatMap(data -> {
                    data.setStock(stock);
                    return branchProductR2dbcRepository.save(data);
                })
                .flatMap(saved -> productR2dbcRepository.findById(saved.getProductId())
                        .map(productData -> mapper.toDomain(saved, productData.getName()))
                );
    }
}
