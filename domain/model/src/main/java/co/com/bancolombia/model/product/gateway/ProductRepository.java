package co.com.bancolombia.model.product.gateway;

import co.com.bancolombia.model.product.Product;
import reactor.core.publisher.Mono;

public interface ProductRepository {

    Mono<Product> findByName(String name);

    Mono<Product> save(Product product);

    Mono<Product> findById(Long id);

    Mono<Boolean> existsByName(String name);

    Mono<Product> updateName(Long id, String newName);
}
