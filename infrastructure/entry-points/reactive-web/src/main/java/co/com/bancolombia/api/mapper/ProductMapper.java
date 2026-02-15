package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.ProductResponse;
import co.com.bancolombia.model.product.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);
}
