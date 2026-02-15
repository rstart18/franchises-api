package co.com.bancolombia.model.branchproduct.gateway;

import co.com.bancolombia.model.branchproduct.StockAlert;
import reactor.core.publisher.Mono;

public interface StockAlertGateway {
    Mono<Void> sendAlert(StockAlert alert);
}
