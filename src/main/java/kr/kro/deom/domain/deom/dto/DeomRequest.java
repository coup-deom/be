package kr.kro.deom.domain.deom.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DeomRequest(
        @NotNull Long storeId, @NotNull String name, @Positive Integer requiredStampAmount) {}
