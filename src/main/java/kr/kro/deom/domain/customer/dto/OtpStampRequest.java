package kr.kro.deom.domain.customer.dto;

import jakarta.validation.constraints.NotNull;
import kr.kro.deom.domain.otp.entity.OtpType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OtpStampRequest {
  @NotNull(message = "사용자 ID는 필수입니다")
  private Long userId;
  @NotNull(message = "가게 ID는 필수입니다")
  private Long storeId;
  @NotNull(message = "otp 타입은 필수입니다")
  private OtpType type;
}
