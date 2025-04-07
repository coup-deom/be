package kr.kro.deom.domain.customer.dto;

import kr.kro.deom.domain.otp.entity.OtpType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OtpStampRequest {
  private Long userId;
  private Long storeId;
  private OtpType type;
}
