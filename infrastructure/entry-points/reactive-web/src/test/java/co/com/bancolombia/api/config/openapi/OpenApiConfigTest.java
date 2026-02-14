package co.com.bancolombia.api.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigTest {

    @Test
    @DisplayName("Should create OpenAPI bean with correct title and version")
    void shouldCreateOpenApiBeanWithCorrectInfo() {
        OpenApiConfig config = new OpenApiConfig();
        OpenAPI openAPI = config.customOpenAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("Franchises API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    @DisplayName("Should include at least one server in the OpenAPI spec")
    void shouldIncludeLocalDevServer() {
        OpenApiConfig config = new OpenApiConfig();
        OpenAPI openAPI = config.customOpenAPI();

        assertNotNull(openAPI.getServers());
        assertEquals(1, openAPI.getServers().size());
        assertEquals("http://localhost:8080", openAPI.getServers().get(0).getUrl());
    }
}
