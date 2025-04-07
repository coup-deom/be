package kr.kro.deom.domain.customer.dto;

import kr.kro.deom.domain.otp.entity.OtpType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OtpDeomRequest {
  private Long userId;
  private Long storeId;
  private OtpType type;
  private Long deomId;
  private String usedStampAmount;
}
