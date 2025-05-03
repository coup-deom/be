package kr.kro.deom.domain.exchange.entity;

import jakarta.persistence.*;
import kr.kro.deom.common.global.entity.BaseTimeEntity;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stamp_exchange")
public class StampExchange extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "responder_id")
    private Long responderId;

    @Column(name = "source_store_id", nullable = false)
    private Long sourceStoreId;

    @Column(name = "target_store_id", nullable = false)
    private Long targetStoreId;

    @Column(name = "source_amount", nullable = false)
    private Integer sourceAmount;

    @Column(name = "target_amount", nullable = false)
    private Integer targetAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    public enum Status {
        PENDING,
        COMPLETED
    }

    public void changeAmounts(Integer newSourceAmount, Integer newTargetAmount) {
        this.sourceAmount = newSourceAmount;
        this.targetAmount = newTargetAmount;
    }
}
