package kr.kro.deom.domain.exchange.dto;

import kr.kro.deom.domain.exchange.entity.StampExchange;
import kr.kro.deom.domain.store.entity.Store;

import java.time.Instant;

public record StampExchangeResponse(
        Long id,
        Instant updatedAt,
        Long creatorId,
        Long responderId,
        Long sourceStoreId,
        String sourceStoreName,
        String sourceBranchName,
        Long targetStoreId,
        String targetStoreName,
        String targetBranchName,
        Integer sourceAmount,
        Integer targetAmount
) {
    public static StampExchangeResponse from(StampExchange exchange, Store sourceStore, Store targetStore) {
        return new StampExchangeResponse(
                exchange.getId(),
                exchange.getUpdatedAt(),
                exchange.getCreatorId(),
                exchange.getResponderId(),
                sourceStore.getId(),
                sourceStore.getStoreName(),
                sourceStore.getBranchName(),
                targetStore.getId(),
                targetStore.getStoreName(),
                targetStore.getBranchName(),
                exchange.getSourceAmount(),
                exchange.getTargetAmount()
        );
    }
}
