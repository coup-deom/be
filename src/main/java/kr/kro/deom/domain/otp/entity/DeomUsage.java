package kr.kro.deom.domain.otp.entity;

import jakarta.persistence.*;
import kr.kro.deom.common.global.entity.BaseTimeEntity;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeomUsage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "used_stamp_amount", nullable = false)
    private Integer usedStampAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;
}
