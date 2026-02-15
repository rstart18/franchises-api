package co.com.bancolombia.adapter.notification;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotificationConfigTest {

    private NotificationConfig config;

    @BeforeEach
    void setUp() {
        config = new NotificationConfig();
    }

    @Test
    @DisplayName("Should create WebClient bean with base URL")
    void shouldCreateWebClient() {
        WebClient webClient = config.notificationWebClient("http://localhost:8081");
        assertNotNull(webClient);
    }

    @Test
    @DisplayName("Should create CircuitBreakerRegistry with notificationService instance")
    void shouldCreateCircuitBreakerRegistry() {
        CircuitBreakerRegistry registry = config.notificationCircuitBreakerRegistry();
        assertNotNull(registry);
        assertNotNull(registry.circuitBreaker("notificationService"));
    }

    @Test
    @DisplayName("Should create RetryRegistry with notificationService instance")
    void shouldCreateRetryRegistry() {
        RetryRegistry registry = config.notificationRetryRegistry();
        assertNotNull(registry);
        assertNotNull(registry.retry("notificationService"));
    }
}
