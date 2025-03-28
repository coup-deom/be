package kr.kro.deom.common.exception.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InvalidRequestMessages {
  // OTP
  INVALID_OTP_CODE("유효하지 않은 otp 코드입니다."),
  EXPIRED_OTP("opt가 만료되었습니다.");

  // 고객

  // 사장

  // 쿠폰(개수미달등)

  // 거래

  private final String message;
}
