package kr.kro.deom.domain.otp.service;

import kr.kro.deom.domain.otp.dto.OtpRedisDto;

public interface OtpOwnerService {
    boolean approveOtp(Long otpCode, Long userId, Long storeId);

    boolean rejectOtp(Long otpCode, Long userId, Long storeId);

    /** Verify OTP - 사장님이 otp입력 받았을 때 여기 조회해서 otp정보 받으면 됩 */
    OtpRedisDto verifyOtp(Long otpCode, Long userId, Long storeId);
}
