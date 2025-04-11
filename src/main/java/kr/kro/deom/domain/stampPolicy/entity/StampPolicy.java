package kr.kro.deom.domain.stampPolicy.entity;

import jakarta.persistence.*;
import java.time.Instant;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.stampPolicy.exception.StampPolicyException;
import lombok.*;

@Entity
@Table(name = "stamp_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StampPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "base_amount", nullable = false)
    private Integer baseAmount;

    @Column(name = "stamp_count", nullable = false)
    private Integer stampCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private StampPolicy(Long storeId, Integer baseAmount, Integer stampCount) {
        validateBaseAmount(baseAmount);
        validateStampCount(stampCount);

        this.storeId = storeId;
        this.baseAmount = baseAmount;
        this.stampCount = stampCount;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static StampPolicy create(Long storeId, int baseAmount, int stampCount) {
        return new StampPolicy(storeId, baseAmount, stampCount);
    }

    private void validateBaseAmount(Integer baseAmount) {
        if (baseAmount == null || baseAmount <= 0) {
            throw new StampPolicyException(CommonErrorCode.INVALID_BASE_AMOUNT);
        }
    }

    private void validateStampCount(Integer stampCount) {
        if (stampCount == null || stampCount <= 0) {
            throw new StampPolicyException(CommonErrorCode.INVALID_STAMP_COUNT);
        }
    }

    public void update(Integer baseAmount, Integer stampCount) {
        validateBaseAmount(baseAmount);
        validateStampCount(stampCount);
        this.baseAmount = baseAmount;
        this.stampCount = stampCount;
        this.updatedAt = Instant.now();
    }

    public void delete() {
        this.deletedAt = Instant.now();
    }
}
