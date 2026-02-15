package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.config.RequestValidator;
import co.com.bancolombia.api.dto.BranchRequest;
import co.com.bancolombia.api.dto.FranchiseRequest;
import co.com.bancolombia.usecase.franchise.AddBranchToFranchiseUseCase;
import co.com.bancolombia.usecase.franchise.CreateFranchiseUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class FranchiseHandler {

    private final CreateFranchiseUseCase createFranchiseUseCase;
    private final FranchiseMapper franchiseMapper;
    private final RequestValidator validator;
    private final AddBranchToFranchiseUseCase addBranchToFranchiseUseCase;
    private final BranchMapper branchMapper;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return request.bodyToMono(FranchiseRequest.class)
                .flatMap(validator::validate)
                .map(franchiseMapper::toDomain)
                .flatMap(createFranchiseUseCase::execute)
                .map(franchiseMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response))
                .doOnSuccess(v -> log.info("Franchise created successfully"))
                .doOnError(e -> log.error("Error creating franchise: {}", e.getMessage()));
    }

    public Mono<ServerResponse> addBranch(ServerRequest request) {
        Long franchiseId = Long.valueOf(request.pathVariable("franchiseId"));
        return request.bodyToMono(BranchRequest.class)
                .flatMap(validator::validate)
                .map(branchMapper::toDomain)
                .flatMap(branch -> addBranchToFranchiseUseCase.execute(franchiseId, branch))
                .map(branchMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response))
                .doOnSuccess(v -> log.info("Branch added to franchise {} successfully", franchiseId))
                .doOnError(e -> log.error("Error adding branch to franchise {}: {}", franchiseId, e.getMessage()));
    }
}
