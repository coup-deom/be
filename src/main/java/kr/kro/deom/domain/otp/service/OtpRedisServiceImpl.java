package kr.kro.deom.domain.otp.service;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpType;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpRedisServiceImpl implements OtpRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveOtpToRedis(Long otpCode, OtpRedisDto otpRedisDto, long ttlSeconds) {
        redisTemplate
                .opsForValue()
                .set(otpCode.toString(), otpRedisDto, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public OtpRedisDto getOtpFromRedis(Long otpCode) {
        Object value = redisTemplate.opsForValue().get(otpCode.toString());
        if (value == null) {
            return null;
        }

        if (value instanceof OtpRedisDto) {
            return (OtpRedisDto) value;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(value, OtpRedisDto.class);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void deleteOtpFromRedis(Long otpCode) {
        redisTemplate.delete(otpCode.toString());
    }

}
