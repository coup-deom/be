package kr.kro.deom.domain.otp.dto;

import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OtpVerifyResponse {
  private Long otpId;
  private OtpType type;
  private OtpStatus status;
}
