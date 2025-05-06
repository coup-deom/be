package kr.kro.deom.domain.exchange.service;

import kr.kro.deom.domain.exchange.dto.StampExchangeResponse;
import kr.kro.deom.domain.exchange.entity.StampExchange;
import kr.kro.deom.domain.store.entity.Store;

public class StampExchangeJoinProjection {
    private final StampExchange exchange;
    private final Store sourceStore;
    private final Store targetStore;

    public StampExchangeJoinProjection(
            StampExchange exchange, Store sourceStore, Store targetStore) {
        this.exchange = exchange;
        this.sourceStore = sourceStore;
        this.targetStore = targetStore;
    }

    public StampExchangeResponse toResponse() {
        return StampExchangeResponse.from(exchange, sourceStore, targetStore);
    }
}
