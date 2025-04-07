package kr.kro.deom.domain.otp.service;

import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.entity.OtpType;
import kr.kro.deom.domain.otp.entity.OtpUsage;

public interface OtpRedisService {
    Long generateOtp(Long userId, Long storeId, OtpType type, Long deomId, Integer usedStampAmount);
    OtpRedisDto verifyOtp(Long otpCode, Long userId, Long storeId);
    boolean approveOtp(Long otpCode, Long userId, Long storeId);
    boolean rejectOtp(Long otpCode, Long userId, Long storeId);
}
