package kr.kro.deom.domain.deom.entity;

import jakarta.persistence.*;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.global.entity.BaseTimeEntity;
import kr.kro.deom.domain.deom.exception.DeomException;
import lombok.*;

@Entity
@Table(name = "service")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "required_stamp_amount", nullable = false)
    private Integer requiredStampAmount;

    private Deom(Long storeId, String name, Integer requiredStampAmount) {
        validateName(name);
        validateRequiredStampAmount(requiredStampAmount);

        this.storeId = storeId;
        this.name = name;
        this.requiredStampAmount = requiredStampAmount;
    }

    public static Deom create(Long storeId, String name, Integer requiredStampAmount) {
        return new Deom(storeId, name, requiredStampAmount);
    }

    private void validateName(String name) {
        if (name == null) {
            throw new DeomException(CommonErrorCode.INVALID_DEOM_NAME);
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
    }
}
