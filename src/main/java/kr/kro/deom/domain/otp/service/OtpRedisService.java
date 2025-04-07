package kr.kro.deom.domain.otp.service;

import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.entity.OtpType;
import kr.kro.deom.domain.otp.entity.OtpUsage;

public interface OtpRedisService {
    void saveOtpToRedis(Long otpCode, OtpRedisDto otpRedisDto, long ttlSeconds);

    OtpRedisDto getOtpFromRedis(Long otpCode);

    void deleteOtpFromRedis(Long otpCode);

}