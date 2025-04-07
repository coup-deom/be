package kr.kro.deom.domain.otp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpRedisDto {
  private Long userId;
  private Long storeId;
  private String type; // "stamp" 또는 "deom"
  private Long deomId; // null 가능
  private String usedStampAmount; // 숫자지만 문자열로 저장됨
  private String createdAt;
}
