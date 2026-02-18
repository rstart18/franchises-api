package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.exception.DomainErrorCode;
import co.com.bancolombia.model.product.Product;
import co.com.bancolombia.model.product.gateway.ProductRepository;
import co.com.bancolombia.model.usecase.UpdateProductNamePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateProductNameUseCase extends UpdateProductNamePort {

    private final ProductRepository productRepository;

    @Override
    public Mono<Product> execute(Long productId, String newName) {
        return Mono.justOrEmpty(newName)
                .filter(name -> !name.isBlank())
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.PRODUCT_NAME_REQUIRED)))
                .flatMap(name -> productRepository.findById(productId))
                .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.PRODUCT_NOT_FOUND)))
                .flatMap(product -> productRepository.existsByName(newName)
                        .filter(exists -> !exists)
                        .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.PRODUCT_NAME_ALREADY_EXISTS)))
                        .then(Mono.defer(() -> productRepository.updateName(productId, newName)))
                );
    }
}
