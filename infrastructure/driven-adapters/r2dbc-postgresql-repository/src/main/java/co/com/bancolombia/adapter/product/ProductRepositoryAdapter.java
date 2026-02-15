package co.com.bancolombia.adapter.product;

import co.com.bancolombia.model.product.Product;
import co.com.bancolombia.model.product.gateway.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductR2dbcRepository productR2dbcRepository;
    private final ProductDataMapper mapper;

    @Override
    public Mono<Product> findByName(String name) {
        return productR2dbcRepository.findByName(name)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Product> save(Product product) {
        return productR2dbcRepository.save(mapper.toData(product))
                .map(mapper::toDomain);
    }
}
