package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.branch.gateway.BranchRepository;
import co.com.bancolombia.model.branchproduct.TopStockProduct;
import co.com.bancolombia.model.branchproduct.gateway.BranchProductRepository;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.franchise.gateway.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetTopStockProductsUseCase {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final BranchProductRepository branchProductRepository;

    public Flux<TopStockProduct> execute(Long franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.FRANCHISE_NOT_FOUND)))
                .flatMapMany(franchise -> branchRepository.findAllByFranchiseId(franchiseId))
                .flatMap(branch -> branchProductRepository.findTopStockByBranch(branch.id())
                        .map(bp -> new TopStockProduct(bp.productId(), bp.productName(),
                                bp.stock(), branch.id(), branch.name()))
                );
    }
}
