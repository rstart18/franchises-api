package co.com.bancolombia.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    private final String url;
    private final String user;
    private final String password;
    private final String[] locations;

    public FlywayConfig(
            @Value("${spring.flyway.url}") String url,
            @Value("${spring.flyway.user}") String user,
            @Value("${spring.flyway.password}") String password,
            @Value("${spring.flyway.locations:classpath:db/migration}") String[] locations) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.locations = locations;
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        return Flyway.configure()
                .dataSource(url, user, password)
                .locations(locations)
                .baselineOnMigrate(true)
                .load();
    }
}
