package kr.kro.deom.domain.stampPolicy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StampPolicyUpdateRequest(
        @NotNull Long storeId, @Positive int baseAmount, @Positive int stampCount) {}
