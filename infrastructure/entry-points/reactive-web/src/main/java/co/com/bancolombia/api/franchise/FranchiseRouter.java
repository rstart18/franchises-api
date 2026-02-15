package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.dto.AddProductToBranchRequest;
import co.com.bancolombia.api.dto.BranchProductResponse;
import co.com.bancolombia.api.dto.BranchRequest;
import co.com.bancolombia.api.dto.BranchResponse;
import co.com.bancolombia.api.dto.ErrorResponse;
import co.com.bancolombia.api.dto.FranchiseRequest;
import co.com.bancolombia.api.dto.FranchiseResponse;
import co.com.bancolombia.api.dto.TopStockProductResponse;
import co.com.bancolombia.api.dto.UpdateBranchNameRequest;
import co.com.bancolombia.api.dto.UpdateFranchiseNameRequest;
import co.com.bancolombia.api.dto.UpdateStockRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
            ),
            @RouterOperation(
                    path = "/api/v1/franchises/{franchiseId}/branches",
                    method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "addBranch",
                    operation = @Operation(
                            operationId = "addBranchToFranchise",
                            summary = "Add a branch to a franchise",
                            description = "Creates a new branch and associates it with an existing franchise.",
                            tags = {"Franchises"},
                            parameters = {
                                    @Parameter(
                                            name = "franchiseId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the franchise",
                                            schema = @Schema(type = "integer", format = "int64")
                                    )
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = BranchRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Branch added successfully",
                                            content = @Content(schema = @Schema(implementation = BranchResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Validation error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/branches/{branchId}/products",
                    method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "addProduct",
                    operation = @Operation(
                            operationId = "addProductToBranch",
                            summary = "Add a product to a branch",
                            description = "Creates a new product (or reuses existing) and associates it with a branch. Each branch can have different stock for the same product.",
                            tags = {"Branches"},
                            parameters = {
                                    @Parameter(
                                            name = "branchId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the branch",
                                            schema = @Schema(type = "integer", format = "int64")
                                    )
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = AddProductToBranchRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Product added to branch successfully",
                                            content = @Content(schema = @Schema(implementation = BranchProductResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Validation error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/branches/{branchId}/products/{productId}",
                    method = RequestMethod.DELETE,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "removeProduct",
                    operation = @Operation(
                            operationId = "removeProductFromBranch",
                            summary = "Remove a product from a branch",
                            description = "Performs a soft delete of the product-branch association. The product itself is not deleted.",
                            tags = {"Branches"},
                            parameters = {
                                    @Parameter(
                                            name = "branchId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the branch",
                                            schema = @Schema(type = "integer", format = "int64")
                                    ),
                                    @Parameter(
                                            name = "productId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the product",
                                            schema = @Schema(type = "integer", format = "int64")
                                    )
                            },
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Product removed from branch successfully"),
                                    @ApiResponse(responseCode = "404", description = "Branch or product association not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/branches/{branchId}",
                    method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "updateBranchName",
                    operation = @Operation(
                            operationId = "updateBranchName",
                            summary = "Update a branch name",
                            description = "Updates the name of an existing branch.",
                            tags = {"Branches"},
                            parameters = {
                                    @Parameter(
                                            name = "branchId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the branch",
                                            schema = @Schema(type = "integer", format = "int64")
                                    )
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = UpdateBranchNameRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Branch name updated successfully",
                                            content = @Content(schema = @Schema(implementation = BranchResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Validation error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Branch name already exists",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/branches/{branchId}/products/{productId}/stock",
                    method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "updateStock",
                    operation = @Operation(
                            operationId = "updateProductStock",
                            summary = "Update product stock in a branch",
                            description = "Updates the stock quantity of a product associated with a specific branch.",
                            tags = {"Branches"},
                            parameters = {
                                    @Parameter(
                                            name = "branchId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the branch",
                                            schema = @Schema(type = "integer", format = "int64")
                                    ),
                                    @Parameter(
                                            name = "productId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the product",
                                            schema = @Schema(type = "integer", format = "int64")
                                    )
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = UpdateStockRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Stock updated successfully",
                                            content = @Content(schema = @Schema(implementation = BranchProductResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Validation error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch or product association not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/franchises/{franchiseId}",
                    method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "updateFranchiseName",
                    operation = @Operation(
                            operationId = "updateFranchiseName",
                            summary = "Update a franchise name",
                            description = "Updates the name of an existing franchise.",
                            tags = {"Franchises"},
                            parameters = {
                                    @Parameter(
                                            name = "franchiseId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the franchise",
                                            schema = @Schema(type = "integer", format = "int64")
                                    )
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = UpdateFranchiseNameRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Franchise name updated successfully",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Validation error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Franchise name already exists",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/franchises/{franchiseId}/products/top-stock",
                    method = RequestMethod.GET,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "getTopStockProducts",
                    operation = @Operation(
                            operationId = "getTopStockProducts",
                            summary = "Get top stock product per branch",
                            description = "Returns the product with the highest stock for each branch of a specific franchise.",
                            tags = {"Franchises"},
                            parameters = {
                                    @Parameter(
                                            name = "franchiseId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the franchise",
                                            schema = @Schema(type = "integer", format = "int64")
                                    )
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Top stock products retrieved successfully",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TopStockProductResponse.class)))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }
                    )
            )
    })
    @Bean
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return RouterFunctions.route()
                .path("/api/v1/franchises", builder -> builder
                        .POST("", handler::createFranchise)
                        .POST("/{franchiseId}/branches", handler::addBranch)
                        .PATCH("/{franchiseId}", handler::updateFranchiseName)
                        .GET("/{franchiseId}/products/top-stock", handler::getTopStockProducts))
                .path("/api/v1/branches", builder -> builder
                        .POST("/{branchId}/products", handler::addProduct)
                        .DELETE("/{branchId}/products/{productId}", handler::removeProduct)
                        .PATCH("/{branchId}/products/{productId}/stock", handler::updateStock)
                        .PATCH("/{branchId}", handler::updateBranchName))
                .build();
    }
}
