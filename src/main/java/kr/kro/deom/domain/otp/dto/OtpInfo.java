package kr.kro.deom.domain.otp.dto;

import java.time.Instant;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpInfo {
  private String userId;
  private String storeId;
  private String type; // "stamp" 또는 "deom"
  private String deomId; // null 가능
  private String usedStampAmount; // 숫자지만 문자열로 저장됨
  private Instant createdAt;
}
