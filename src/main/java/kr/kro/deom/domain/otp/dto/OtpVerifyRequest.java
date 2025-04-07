package kr.kro.deom.domain.otp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyRequest {

    @NotNull(message = "OTP 코드는 필수입니다.")
    private Long otpCode;
}
