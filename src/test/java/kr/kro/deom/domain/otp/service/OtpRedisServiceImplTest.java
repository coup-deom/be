package kr.kro.deom.domain.otp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.entity.OtpType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class OtpRedisServiceImplTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;

    @Mock private ValueOperations<String, Object> valueOperations;

    @InjectMocks private OtpRedisServiceImpl otpRedisService;

    private final Long TEST_OTP_CODE = 1234L;
    private final Long TEST_STORE_ID = 1L;
    private final Long TEST_USER_ID = 10L;
    private final Long TEST_DEOM_ID = 5L;
    private final Integer TEST_USED_STAMP_AMOUNT = 3;
    private final OtpType TEST_OTP_TYPE = OtpType.STAMP;
    private final long TEST_TTL_SECONDS = 10800L;

    @Test
    @DisplayName("Redis에 OTP 정보 저장 성공")
    void saveOtpToRedis_Success() {
        // given
        OtpRedisDto otpRedisDto = createTestOtpRedisDto();
        String redisKey = TEST_STORE_ID + ":" + TEST_OTP_CODE;

        // when
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        otpRedisService.saveOtpToRedis(TEST_OTP_CODE, otpRedisDto, TEST_TTL_SECONDS);

        // then
        verify(valueOperations).set(redisKey, otpRedisDto, TEST_TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Redis에서 OTP 정보 조회 성공 - OtpRedisDto 타입")
    void getOtpFromRedis_Success_OtpRedisDto() {
        // given
        OtpRedisDto otpRedisDto = createTestOtpRedisDto();
        String redisKey = TEST_STORE_ID + ":" + TEST_OTP_CODE;

        // when
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(otpRedisDto);
        OtpRedisDto result = otpRedisService.getOtpFromRedis(TEST_OTP_CODE, TEST_STORE_ID);

        // then
        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(TEST_STORE_ID, result.getStoreId());
        assertEquals(TEST_OTP_TYPE, result.getType());
        assertEquals(TEST_DEOM_ID, result.getDeomId());
        assertEquals(TEST_USED_STAMP_AMOUNT, result.getUsedStampAmount());

        verify(valueOperations).get(redisKey);
    }

    @Test
    @DisplayName("Redis에서 OTP 정보 조회 성공 - Map 타입 변환")
    void getOtpFromRedis_Success_MapConversion() {
        // given
        String redisKey = TEST_STORE_ID + ":" + TEST_OTP_CODE;
        Map<String, Object> mapValue = new HashMap<>();
        mapValue.put("userId", TEST_USER_ID);
        mapValue.put("storeId", TEST_STORE_ID);
        mapValue.put("type", TEST_OTP_TYPE.name());
        mapValue.put("deomId", TEST_DEOM_ID);
        mapValue.put("usedStampAmount", TEST_USED_STAMP_AMOUNT);
        mapValue.put("createdAt", Instant.now());

        // when
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(mapValue);
        OtpRedisDto result = otpRedisService.getOtpFromRedis(TEST_OTP_CODE, TEST_STORE_ID);

        // then
        assertNotNull(result);
        verify(valueOperations).get(redisKey);
    }

    @Test
    @DisplayName("Redis에서 OTP 정보가 없을 경우 null 반환")
    void getOtpFromRedis_NotFound() {
        // given
        String redisKey = TEST_STORE_ID + ":" + TEST_OTP_CODE;

        // when
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(null);
        OtpRedisDto result = otpRedisService.getOtpFromRedis(TEST_OTP_CODE, TEST_STORE_ID);

        // then
        assertNull(result);
        verify(valueOperations).get(redisKey);
    }

    @Test
    @DisplayName("Redis에서 OTP 정보 삭제 성공")
    void deleteOtpFromRedis_Success() {
        // given
        String redisKey = TEST_STORE_ID + ":" + TEST_OTP_CODE;

        // when
        otpRedisService.deleteOtpFromRedis(TEST_OTP_CODE, TEST_STORE_ID);

        // then
        verify(redisTemplate).delete(redisKey);
    }

    private OtpRedisDto createTestOtpRedisDto() {
        return OtpRedisDto.builder()
                .userId(TEST_USER_ID)
                .storeId(TEST_STORE_ID)
                .type(TEST_OTP_TYPE)
                .deomId(TEST_DEOM_ID)
                .usedStampAmount(TEST_USED_STAMP_AMOUNT)
                .createdAt(Instant.now())
                .build();
    }
}
