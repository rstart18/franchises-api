package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.branchproduct.TopStockProduct;
import co.com.bancolombia.model.branchproduct.gateway.BranchProductRepository;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.franchise.gateway.FranchiseRepository;
import co.com.bancolombia.model.usecase.GetTopStockProductsPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetTopStockProductsUseCase extends GetTopStockProductsPort {

    private final FranchiseRepository franchiseRepository;
    private final BranchProductRepository branchProductRepository;

    @Override
    public Flux<TopStockProduct> execute(Long franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.FRANCHISE_NOT_FOUND)))
                .flatMapMany(franchise -> branchProductRepository.findTopStockByFranchise(franchiseId));
    }
}
