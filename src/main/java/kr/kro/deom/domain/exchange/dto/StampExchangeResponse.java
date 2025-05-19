package kr.kro.deom.domain.exchange.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import kr.kro.deom.domain.exchange.entity.StampExchange;
import kr.kro.deom.domain.exchange.entity.StampExchangeStatus;
import kr.kro.deom.domain.store.entity.Store;

public record StampExchangeResponse(
        Long id,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd'T'HH:mm:ssXXX",
                        timezone = "Asia/Seoul")
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
        Integer targetAmount,
        StampExchangeStatus status) {
    public static StampExchangeResponse from(
            StampExchange exchange, Store sourceStore, Store targetStore) {
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
                exchange.getTargetAmount(),
                exchange.getStatus());
    }
}
