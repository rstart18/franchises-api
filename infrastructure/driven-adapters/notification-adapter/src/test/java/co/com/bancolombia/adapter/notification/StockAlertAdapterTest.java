package co.com.bancolombia.adapter.notification;

import co.com.bancolombia.model.branchproduct.StockAlert;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class StockAlertAdapterTest {

    private MockWebServer mockServer;
    private StockAlertAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockServer.url("/").toString())
                .build();

        adapter = new StockAlertAdapter(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    @DisplayName("Should send alert successfully when notification service responds 200")
    void shouldSendAlertSuccessfully() {
        mockServer.enqueue(new MockResponse().setResponseCode(200));

        StockAlert alert = new StockAlert(10L, 1L, "Laptop", 3);

        StepVerifier.create(adapter.sendAlert(alert))
                .verifyComplete();

        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    @DisplayName("Should propagate error when notification service responds 500")
    void shouldPropagateErrorOnServerError() {
        mockServer.enqueue(new MockResponse().setResponseCode(500));

        StockAlert alert = new StockAlert(10L, 1L, "Laptop", 3);

        StepVerifier.create(adapter.sendAlert(alert))
                .expectError()
                .verify();

        assertEquals(1, mockServer.getRequestCount());
    }

    @Test
    @DisplayName("Fallback should return empty Mono on generic exception")
    void fallbackShouldReturnEmptyOnException() {
        StockAlert alert = new StockAlert(10L, 1L, "Laptop", 3);
        Exception exception = new RuntimeException("Connection refused");

        StepVerifier.create(adapter.fallback(alert, exception))
                .verifyComplete();
    }

    @Test
    @DisplayName("Fallback should return empty Mono on CallNotPermittedException")
    void fallbackShouldReturnEmptyOnCircuitOpen() {
        StockAlert alert = new StockAlert(10L, 1L, "Laptop", 3);
        CallNotPermittedException callNotPermittedException = mock(CallNotPermittedException.class);

        StepVerifier.create(adapter.fallback(alert, callNotPermittedException))
                .verifyComplete();
    }
}
