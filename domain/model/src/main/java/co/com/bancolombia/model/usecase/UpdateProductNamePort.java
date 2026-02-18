package co.com.bancolombia.model.usecase;

import co.com.bancolombia.model.product.Product;
import reactor.core.publisher.Mono;

public abstract class UpdateProductNamePort {
    public abstract Mono<Product> execute(Long productId, String newName);
}
