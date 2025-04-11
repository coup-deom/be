package kr.kro.deom.domain.stampPolicy.dto;

import kr.kro.deom.domain.stampPolicy.entity.StampPolicy;

public record StampPolicyResponse(Long id, Long storeId, int baseAmount, int stampCount) {
    public static StampPolicyResponse from(StampPolicy entity) {
        return new StampPolicyResponse(
                entity.getId(),
                entity.getStoreId(),
                entity.getBaseAmount(),
                entity.getStampCount());
    }
}
