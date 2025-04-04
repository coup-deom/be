package kr.kro.deom.domain.otp.service;

import java.util.concurrent.TimeUnit;
import kr.kro.deom.domain.otp.dto.OtpInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpRedisService {

  private static final String OTP_KEY_PREFIX = "otp:";

  private final RedisTemplate<String, Object> redisTemplate;

  public void saveOtp(String otpCode, OtpInfo otpInfo, long ttlSeconds) {
    String key = OTP_KEY_PREFIX + otpCode;
    redisTemplate.opsForValue().set(key, otpInfo, ttlSeconds, TimeUnit.SECONDS);
  }

  public OtpInfo getOtp(String otpCode) {
    String key = OTP_KEY_PREFIX + otpCode;
    Object value = redisTemplate.opsForValue().get(key);
    return value instanceof OtpInfo ? (OtpInfo) value : null;
  }

  public void deleteOtp(String otpCode) {
    redisTemplate.delete(OTP_KEY_PREFIX + otpCode);
  }
}
