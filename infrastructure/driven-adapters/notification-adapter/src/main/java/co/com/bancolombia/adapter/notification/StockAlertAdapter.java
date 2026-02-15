package co.com.bancolombia.adapter.notification;

import co.com.bancolombia.model.branchproduct.StockAlert;
import co.com.bancolombia.model.branchproduct.gateway.StockAlertGateway;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class StockAlertAdapter implements StockAlertGateway {

    private static final String INSTANCE_NAME = "notificationService";
    private static final String ALERT_URI = "/alerts/low-stock";
    private static final String SUCCESS_MESSAGE = "Low-stock alert sent for product {} in branch {} (stock: {})";
    private static final String ERROR_MESSAGE = "Failed to send low-stock alert for product {} in branch {} (stock: {}): {}";

    private final WebClient notificationWebClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public StockAlertAdapter(WebClient notificationWebClient,
                             CircuitBreakerRegistry notificationCircuitBreakerRegistry,
                             RetryRegistry notificationRetryRegistry) {
        this.notificationWebClient = notificationWebClient;
        this.circuitBreaker = notificationCircuitBreakerRegistry.circuitBreaker(INSTANCE_NAME);
        this.retry = notificationRetryRegistry.retry(INSTANCE_NAME);
    }

    @Override
    public Mono<Void> sendAlert(StockAlert alert) {
        return notificationWebClient.post()
                .uri(ALERT_URI)
                .bodyValue(alert)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.info(SUCCESS_MESSAGE,
                        alert.productId(), alert.branchId(), alert.currentStock()))
                .then()
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(error -> log.warn(ERROR_MESSAGE,
                        alert.productId(), alert.branchId(), alert.currentStock(), error.getMessage()));
    }
}
