package co.com.bancolombia.model.usecase;

import co.com.bancolombia.model.branchproduct.TopStockProduct;
import reactor.core.publisher.Flux;

public abstract class GetTopStockProductsPort {
    public abstract Flux<TopStockProduct> execute(Long franchiseId);
}
