package kr.kro.deom.domain.otp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 매개변수로 받는 생성자
@Builder
public class DeomUsageDto {
    private Long id;

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @NotNull(message = "가게 ID는 필수입니다")
    private Long storeId;

    @NotNull(message = "OTP코드는 필수입니다")
    private Long otpCode;

    @NotNull(message = "덤 ID는 필수입니다")
    private Long deomId;

    @NotNull(message = "사용 스탬프 수량은 필수입니다")
    @Min(value = 1, message = "사용 스탬프 수량은 최소 1 이상이어야 합니다")
    private Integer usedStampAmount;
}
