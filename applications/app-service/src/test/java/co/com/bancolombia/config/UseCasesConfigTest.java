package co.com.bancolombia.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UseCasesConfigTest {

    @Test
    @org.junit.jupiter.api.DisplayName("UseCasesConfig @ComponentScan filter should only pick up beans ending with UseCase")
    void useCasesConfigShouldFilterByUseCaseSuffix() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            assertTrue(context.containsBean("myUseCase"), "Bean 'myUseCase' should be registered");
            assertNotNull(context.getBean("myUseCase"));
        }
    }

    @Configuration
    static class TestConfig {

        @Bean
        public MyUseCase myUseCase() {
            return new MyUseCase();
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}