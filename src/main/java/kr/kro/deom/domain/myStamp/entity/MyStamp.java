package kr.kro.deom.domain.myStamp.entity;

import jakarta.persistence.*;
import kr.kro.deom.common.global.entity.BaseTimeEntity;
import lombok.*;

@Entity
@Table(name = "my_stamp")
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MyStamp extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "stamp_amount", nullable = false)
    private Integer stampAmount;

    public MyStamp(Long userId, Long storeId, Integer stampAmount) {
        this.userId = userId;
        this.storeId = storeId;
        this.stampAmount = stampAmount;
    }
}
