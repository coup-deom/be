package kr.kro.deom.domain.otp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StampUsageRequest {
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @NotNull(message = "가게 ID는 필수입니다")
    private Long storeId;

    @NotNull(message = "OTP코드는 필수입니다")
    private Long otpCode;

    @NotNull(message = "스탬프 개수는 필수입니다")
    private Long stampAmount;
}
