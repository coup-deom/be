package kr.kro.deom.domain.otp.service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.concurrent.TimeUnit;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpRedisServiceImpl implements OtpRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveOtpToRedis(Long otpCode, OtpRedisDto otpRedisDto, long ttlSeconds) {
        String redisKey = otpRedisDto.getStoreId() + ":" + otpCode;
        redisTemplate.opsForValue().set(redisKey, otpRedisDto, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public OtpRedisDto getOtpFromRedis(Long otpCode, Long storeId) {
        String redisKey = storeId + ":" + otpCode;

        Object value = redisTemplate.opsForValue().get(redisKey);
        if (value == null) {
            return null;
        }

        if (value instanceof OtpRedisDto) {
            return (OtpRedisDto) value;
        }

        ObjectMapper mapper =
                JsonMapper.builder()
                        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                        .addModule(new JavaTimeModule()) // Java 8 날짜/시간 모듈 추가
                        .build();
        return mapper.convertValue(value, OtpRedisDto.class);
    }

    @Override
    public void deleteOtpFromRedis(Long otpCode, Long storeId) {
        String redisKey = storeId + ":" + otpCode;
        redisTemplate.delete(redisKey);
    }
}
