package kr.kro.deom.domain.otp.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpUsage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "otp", nullable = false)
  private Long otp;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "store_id", nullable = false)
  private Long storeId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private OtpType type; // stamp | deom

  @Column(name = "deom_id")
  private Integer deomId;

  @Column(name = "used_stamp_amount")
  private Integer usedStampAmount;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private OtpStatus status; // 진행중, 승인, 거절, 완료
}
