package kr.kro.deom.domain.otp.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpRedisService {

  private static final long OTP_TTL_SECONDS = 60 * 60 * 3;
  private static final Random RANDOM = new Random();

  private final OtpRepository otpRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  public void saveOtpToRedis(Long otpCode, OtpRedisDto otpRedisDto, long ttlSeconds) {
    redisTemplate.opsForValue().set(otpCode.toString(), otpRedisDto, ttlSeconds, TimeUnit.SECONDS);
  }

  private OtpRedisDto getOtpFromRedis(Long otpCode) {
    Object value = redisTemplate.opsForValue().get(otpCode.toString());
    return value instanceof OtpRedisDto ? (OtpRedisDto) value : null;
  }

  private void deleteOtpFromRedis(Long otpCode) {
    redisTemplate.delete(otpCode.toString());
  }
}
