package kr.kro.deom.domain.exchange.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.Map;
import kr.kro.deom.domain.exchange.entity.StampExchange;
import kr.kro.deom.domain.store.entity.Store;

public record StampExchangeExecutionResponse(
        Long exchangeId,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd'T'HH:mm:ssXXX",
                        timezone = "Asia/Seoul")
                Instant completedAt,
        Long sourceStoreId,
        String sourceStoreName,
        Long targetStoreId,
        String targetStoreName,
        Integer sourceAmount,
        Integer targetAmount,
        StampTransactionSummary creatorTransaction,
        StampTransactionSummary responderTransaction) {
    public static StampExchangeExecutionResponse from(
            StampExchange exchange,
            Store sourceStore,
            Store targetStore,
            Long creatorId,
            Long responderId) {

        StampTransactionSummary creatorSummary =
                new StampTransactionSummary(
                        creatorId,
                        Map.of(
                                sourceStore.getId(), -exchange.getSourceAmount(),
                                targetStore.getId(), exchange.getTargetAmount()));

        StampTransactionSummary responderSummary =
                new StampTransactionSummary(
                        responderId,
                        Map.of(
                                sourceStore.getId(), exchange.getSourceAmount(),
                                targetStore.getId(), -exchange.getTargetAmount()));

        return new StampExchangeExecutionResponse(
                exchange.getId(),
                exchange.getUpdatedAt(),
                sourceStore.getId(),
                sourceStore.getStoreName(),
                targetStore.getId(),
                targetStore.getStoreName(),
                exchange.getSourceAmount(),
                exchange.getTargetAmount(),
                creatorSummary,
                responderSummary);
    }
}
