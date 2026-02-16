package co.com.bancolombia.adapter.notification;

import co.com.bancolombia.model.branchproduct.StockAlert;
import co.com.bancolombia.model.branchproduct.gateway.StockAlertGateway;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockAlertAdapter implements StockAlertGateway {

    private static final String INSTANCE_NAME = "notificationService";
    private static final String FALLBACK_METHOD_NAME = "fallback";
    private static final String ALERT_URI = "/alerts/low-stock";
    private static final String SUCCESS_MESSAGE = "Low-stock alert sent for product {} in branch {} (stock: {})";
    private static final String ERROR_MESSAGE = "Failed to send low-stock alert for product {} in branch {} (stock: {}): {}";
    private static final String CIRCUIT_OPEN_MESSAGE = "Circuit breaker is open for notification service. Skipping alert for product {} in branch {} (stock: {})";

    private final WebClient notificationWebClient;

    @Override
    @CircuitBreaker(name = INSTANCE_NAME, fallbackMethod = FALLBACK_METHOD_NAME)
    public Mono<Void> sendAlert(StockAlert alert) {
        return notificationWebClient.post()
                .uri(ALERT_URI)
                .bodyValue(alert)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.info(SUCCESS_MESSAGE,
                        alert.productId(), alert.branchId(), alert.currentStock()))
                .then();
    }

    public Mono<Void> fallback(StockAlert alert, Exception exception) {
        log.warn(ERROR_MESSAGE,
                alert.productId(), alert.branchId(), alert.currentStock(), exception.getMessage());
        return Mono.empty();
    }

    public Mono<Void> fallback(StockAlert alert, CallNotPermittedException callNotPermittedException) {
        log.warn(CIRCUIT_OPEN_MESSAGE,
                alert.productId(), alert.branchId(), alert.currentStock());
        return Mono.empty();
    }
}
