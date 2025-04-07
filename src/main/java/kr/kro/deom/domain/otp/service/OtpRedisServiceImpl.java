package kr.kro.deom.domain.otp.service;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpType;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpRedisServiceImpl implements OtpRedisService {

  private static final long OTP_TTL_SECONDS = 60 * 60 * 3;
  private static final Random RANDOM = new Random();

  private final OtpRepository otpRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public Long generateOtp(Long userId, Long storeId, OtpType type, Long deomId, Integer usedStampAmount) {

    Long otpCode = 1000L + RANDOM.nextInt(9000);

    while (otpRepository.existsByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING)) {
      otpCode = 1000L + RANDOM.nextInt(9000);
    }

    Instant now = Instant.now();

    // PostgreSQL
    OtpUsage otpUsage = OtpUsage.builder()
            .otp(otpCode)
            .userId(userId)
            .storeId(storeId)
            .type(type)
            .deomId(deomId)
            .usedStampAmount(usedStampAmount)
            .createdAt(now)
            .status(OtpStatus.PENDING)
            .build();

    otpRepository.save(otpUsage);

    // Redis
    OtpRedisDto otpRedisDto = OtpRedisDto.builder()
            .userId(userId)
            .storeId(storeId)
            .type(type)
            .deomId(deomId)
            .usedStampAmount(usedStampAmount)
            .createdAt(now)
            .build();

    saveOtpToRedis(otpCode, otpRedisDto, OTP_TTL_SECONDS);

    return otpCode;
  }

  /**
   * Verify OTP - 사장님이 otp입력 받았을 때 여기 조회해서 otp정보 받으면 됩
   */
  @Override
  public OtpRedisDto verifyOtp(Long otpCode,Long userId, Long storeId) {
    // 처음에는 레디스에서 조회
    OtpRedisDto otpRedisDto = getOtpFromRedis(otpCode);

    if (otpRedisDto != null) {
      if (storeId.equals(otpRedisDto.getStoreId())) {
        return otpRedisDto;
      }
      return null;
    }

    // 레디스 장애로 조회 실패한 경우 postgreSQL에서 조회 후 변환 후 반환
    OtpUsage otpUsage = otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);

    if (otpUsage == null || !userId.equals(otpUsage.getUserId())) {
      return null;
    }

    return convertToOtpRedisDto(otpUsage);

  }


  @Override
  public boolean approveOtp(Long otpCode, Long userId, Long storeId) {
    // PostgreSQL
    OtpUsage otpUsage = otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);

    if (otpUsage == null ||  !userId.equals(otpUsage.getUserId())) {
      return false;

    }

    otpUsage.setStatus(OtpStatus.APPROVED);
    otpRepository.save(otpUsage);

    //redis
    deleteOtpFromRedis(otpCode);

    return true;
  }


  @Override
  public boolean rejectOtp(Long otpCode, Long userId, Long storeId) {
    //PostgreSQL
    OtpUsage otpUsage = otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);

    if (otpUsage == null || !userId.equals(otpUsage.getUserId())) {
      return false;
    }

    otpUsage.setStatus(OtpStatus.REJECTED);
    otpRepository.save(otpUsage);

    // redis
    deleteOtpFromRedis(otpCode);

    return true;

  }

  private void saveOtpToRedis(Long otpCode, OtpRedisDto otpRedisDto, long ttlSeconds) {
    redisTemplate.opsForValue().set(otpCode.toString(), otpRedisDto, ttlSeconds, TimeUnit.SECONDS);
  }


  private OtpRedisDto getOtpFromRedis(Long otpCode) {
    Object value = redisTemplate.opsForValue().get(otpCode.toString());
    return value instanceof OtpRedisDto ? (OtpRedisDto) value : null;
  }


  private void deleteOtpFromRedis(Long otpCode) {
    redisTemplate.delete(otpCode.toString());
  }

  private OtpRedisDto convertToOtpRedisDto(OtpUsage otpUsage) {
    return OtpRedisDto.builder()
            .userId(otpUsage.getUserId())
            .storeId(otpUsage.getStoreId())
            .type(otpUsage.getType())
            .deomId(otpUsage.getDeomId())
            .usedStampAmount(otpUsage.getUsedStampAmount())
            .createdAt(otpUsage.getCreatedAt())
            .build();
  }
}
