package co.com.bancolombia.api.config;

import co.com.bancolombia.api.FranchiseHandler;
import co.com.bancolombia.api.FranchiseRouter;
import co.com.bancolombia.api.mapper.BranchMapper;
import co.com.bancolombia.api.mapper.BranchProductMapper;
import co.com.bancolombia.api.mapper.FranchiseMapper;
import co.com.bancolombia.api.mapper.ProductMapper;
import co.com.bancolombia.model.usecase.AddBranchToFranchisePort;
import co.com.bancolombia.model.usecase.AddProductToBranchPort;
import co.com.bancolombia.model.usecase.CreateFranchisePort;
import co.com.bancolombia.model.usecase.GetTopStockProductsPort;
import co.com.bancolombia.model.usecase.RemoveProductFromBranchPort;
import co.com.bancolombia.model.usecase.UpdateBranchNamePort;
import co.com.bancolombia.model.usecase.UpdateFranchiseNamePort;
import co.com.bancolombia.model.usecase.UpdateProductNamePort;
import co.com.bancolombia.model.usecase.UpdateProductStockPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {FranchiseRouter.class, FranchiseHandler.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class})
@TestPropertySource(properties = "cors.allowed-origins=http://localhost:3000")
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateFranchisePort createFranchiseUseCase;
    @MockitoBean
    private FranchiseMapper franchiseMapper;
    @MockitoBean
    private RequestValidator requestValidator;
    @MockitoBean
    private AddBranchToFranchisePort addBranchToFranchiseUseCase;
    @MockitoBean
    private BranchMapper branchMapper;
    @MockitoBean
    private AddProductToBranchPort addProductToBranchUseCase;
    @MockitoBean
    private BranchProductMapper branchProductMapper;
    @MockitoBean
    private RemoveProductFromBranchPort removeProductFromBranchUseCase;
    @MockitoBean
    private UpdateProductStockPort updateProductStockUseCase;
    @MockitoBean
    private GetTopStockProductsPort getTopStockProductsUseCase;
    @MockitoBean
    private UpdateFranchiseNamePort updateFranchiseNameUseCase;
    @MockitoBean
    private UpdateBranchNamePort updateBranchNameUseCase;
    @MockitoBean
    private UpdateProductNamePort updateProductNameUseCase;
    @MockitoBean
    private ProductMapper productMapper;
    @MockitoBean
    private HandlerLogger handlerLogger;

    @Test
    void securityHeadersShouldBePresent() {
        when(getTopStockProductsUseCase.execute(any())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/franchises/1/products/top-stock")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

}
