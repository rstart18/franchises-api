package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.dto.ErrorResponse;
import co.com.bancolombia.api.dto.FranchiseRequest;
import co.com.bancolombia.api.dto.FranchiseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class FranchiseRouter {

    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/franchises",
                    method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "createFranchise",
                    operation = @Operation(
                            operationId = "createFranchise",
                            summary = "Create a new franchise",
                            description = "Creates a new franchise identified by name.",
                            tags = {"Franchises"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = FranchiseRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Franchise created successfully",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Validation error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Franchise name already exists",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }
                    )
            )
    })
    @Bean
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return RouterFunctions.route()
                .path("/api/v1/franchises", builder -> builder
                        .POST("", handler::createFranchise))
                .build();
    }
}
