package kr.kro.deom.domain.deom.entity;

import jakarta.persistence.*;
import java.time.Instant;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.deom.exception.DeomException;
import kr.kro.deom.domain.stampPolicy.exception.StampPolicyException;
import lombok.*;

@Entity
@Table(name = "service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Deom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "required_stamp_amount", nullable = false)
    private Integer requiredStampAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private Deom(Long storeId, String name, Integer requiredStampAmount) {
        validateName(name);
        validateRequiredStampAmount(requiredStampAmount);

        this.storeId = storeId;
        this.name = name;
        this.requiredStampAmount = requiredStampAmount;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Deom create(Long storeId, String name, Integer requiredStampAmount) {
        return new Deom(storeId, name, requiredStampAmount);
    }

    private void validateName(String name) {
        if (name == null) {
            throw new StampPolicyException(CommonErrorCode.INVALID_DEOM_NAME);
        }
    }

    private void validateRequiredStampAmount(Integer requiredStampAmount) {
        if (requiredStampAmount == null || requiredStampAmount <= 0) {
            throw new DeomException(CommonErrorCode.INVALID_REQUIRED_STAMP_AMOUNT);
        }
    }

    public void update(String newName, Integer newRequiredStampAmount) {
        validateName(newName);
        validateRequiredStampAmount(newRequiredStampAmount);
        this.name = newName;
        this.requiredStampAmount = newRequiredStampAmount;
        this.updatedAt = Instant.now();
    }

    public void delete() {
        this.deletedAt = Instant.now();
    }
}
