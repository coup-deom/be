package kr.kro.deom.domain.otp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import kr.kro.deom.domain.otp.entity.OtpType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OtpDeomRequest {
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @NotNull(message = "가게 ID는 필수입니다")
    private Long storeId;

    @NotNull(message = "otp 타입은 필수입니다")
    @JsonProperty("type")
    private OtpType type;

    @NotNull(message = "deom ID는 필수입니다")
    private Long deomId;

    @NotNull(message = "사용할 스탬프 수량은 필수입니다")
    private Integer usedStampAmount;
}
