package kr.kro.deom.domain.otp.service;

import kr.kro.deom.domain.otp.dto.OtpRedisDto;

public interface OtpRedisService {
    void saveOtpToRedis(Long otpCode, OtpRedisDto otpRedisDto, long ttlSeconds);

    OtpRedisDto getOtpFromRedis(Long otpCode, Long storeId);

    void deleteOtpFromRedis(Long otpCode, Long storeId);
}
