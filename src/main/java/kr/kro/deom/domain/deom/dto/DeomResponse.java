package kr.kro.deom.domain.deom.dto;

import kr.kro.deom.domain.deom.entity.Deom;

public record DeomResponse(Long id, Long storeId, String name, Integer requiredStampAmount) {
    public static DeomResponse from(Deom entity) {
        return new DeomResponse(
                entity.getId(),
                entity.getStoreId(),
                entity.getName(),
                entity.getRequiredStampAmount());
    }
}
