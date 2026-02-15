package co.com.bancolombia.adapter.notification;

import co.com.bancolombia.model.branchproduct.StockAlert;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockAlertAdapterTest {

    private MockWebServer mockServer;
    private StockAlertAdapter adapter;
    private CircuitBreakerRegistry circuitBreakerRegistry;
    private RetryRegistry retryRegistry;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockServer.url("/").toString())
                .build();

        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(5)
                .minimumNumberOfCalls(3)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .permittedNumberOfCallsInHalfOpenState(1)
                .build();
        circuitBreakerRegistry = CircuitBreakerRegistry.of(cbConfig);

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100))
                .build();
        retryRegistry = RetryRegistry.of(retryConfig);

        adapter = new StockAlertAdapter(webClient, circuitBreakerRegistry, retryRegistry);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    @DisplayName("Should send alert successfully when notification service responds 200")
    void shouldSendAlertSuccessfully() {
        // Need 3 responses: retry will attempt up to 3 times, but first succeeds
        mockServer.enqueue(new MockResponse().setResponseCode(200));

        StockAlert alert = new StockAlert(10L, 1L, "Laptop", 3);

        StepVerifier.create(adapter.sendAlert(alert))
                .verifyComplete();

        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    @DisplayName("Should retry and succeed on second attempt")
    void shouldRetryAndSucceedOnSecondAttempt() {
        mockServer.enqueue(new MockResponse().setResponseCode(500));
        mockServer.enqueue(new MockResponse().setResponseCode(200));

        StockAlert alert = new StockAlert(10L, 1L, "Laptop", 3);

        StepVerifier.create(adapter.sendAlert(alert))
                .verifyComplete();

        assertEquals(2, mockServer.getRequestCount());
    }

    @Test
    @DisplayName("Should fail after exhausting all retry attempts")
    void shouldFailAfterExhaustingRetries() {
        mockServer.enqueue(new MockResponse().setResponseCode(500));
        mockServer.enqueue(new MockResponse().setResponseCode(500));
        mockServer.enqueue(new MockResponse().setResponseCode(500));

        StockAlert alert = new StockAlert(10L, 1L, "Laptop", 3);

        StepVerifier.create(adapter.sendAlert(alert))
                .expectError()
                .verify();

        assertEquals(3, mockServer.getRequestCount());
    }

    @Test
    @DisplayName("Should open circuit breaker after consecutive failures")
    void shouldOpenCircuitBreakerAfterConsecutiveFailures() {
        // Enqueue enough 500 responses to trigger CB (minimumNumberOfCalls=3, failureRate=50%)
        for (int i = 0; i < 15; i++) {
            mockServer.enqueue(new MockResponse().setResponseCode(500));
        }

        StockAlert alert = new StockAlert(10L, 1L, "Laptop", 3);

        // Make enough failing calls to open the circuit breaker
        for (int i = 0; i < 4; i++) {
            StepVerifier.create(adapter.sendAlert(alert))
                    .expectError()
                    .verify();
        }

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("notificationService");
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
    }
}
