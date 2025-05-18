package kr.kro.deom.domain.exchange.dto;

import java.util.Map;

public record StampTransactionSummary(Long userId, Map<Long, Integer> storeStampChanges) {}
