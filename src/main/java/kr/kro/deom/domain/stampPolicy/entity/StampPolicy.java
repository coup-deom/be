package kr.kro.deom.domain.stampPolicy.entity;

import jakarta.persistence.*;
import kr.kro.deom.common.global.entity.BaseTimeEntity;
import lombok.*;

@Entity
@Table(name = "stamp_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StampPolicy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "base_amount", nullable = false)
    private Integer baseAmount;

    @Column(name = "stamp_count", nullable = false)
    private Integer stampCount;
}
