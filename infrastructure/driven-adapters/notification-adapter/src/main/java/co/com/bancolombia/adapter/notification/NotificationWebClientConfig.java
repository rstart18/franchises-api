package co.com.bancolombia.adapter.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class NotificationWebClientConfig {

    @Bean
    public WebClient notificationWebClient(
            @Value("${notification.service.base-url:http://localhost:8081}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
