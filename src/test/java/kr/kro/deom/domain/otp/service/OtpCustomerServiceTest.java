package kr.kro.deom.domain.otp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.dto.request.OtpDeomRequest;
import kr.kro.deom.domain.otp.dto.request.OtpStampRequest;
import kr.kro.deom.domain.otp.dto.response.OtpResponse;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpType;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.exception.OtpException;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class OtpCustomerServiceTest {

    @Mock private OtpRedisService otpRedisService;

    @Mock private OtpRepository otpRepository;

    @Mock private MyStampRepository myStampRepository;

    @InjectMocks private OtpCustomerService otpCustomerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("스탬프 OTP 발급 성공 테스트")
    void issueStampOtpSuccessTest() {
        // given
        OtpStampRequest request = new OtpStampRequest();
        ReflectionTestUtils.setField(request, "userId", 1L);
        ReflectionTestUtils.setField(request, "storeId", 1L);
        ReflectionTestUtils.setField(request, "type", OtpType.STAMP);

        // when
        OtpResponse response = otpCustomerService.issueStampOtp(request);

        // then
        assertNotNull(response);
        assertNotNull(response.getOtpCode());
        verify(otpRedisService).saveOtpToRedis(any(Long.class), any(OtpRedisDto.class), eq(10800L));
        verify(otpRepository).save(any(OtpUsage.class));
    }

    @Test
    @DisplayName("덤 OTP 발급 성공 테스트")
    void issueDeomOtpSuccessTest() {
        // given
        OtpDeomRequest request = new OtpDeomRequest();
        Long userId = 1L;
        Long storeId = 1L;
        Long deomId = 1L;
        Integer usedStampAmount = 5;

        ReflectionTestUtils.setField(request, "userId", userId);
        ReflectionTestUtils.setField(request, "storeId", storeId);
        ReflectionTestUtils.setField(request, "type", OtpType.DEOM);
        ReflectionTestUtils.setField(request, "deomId", deomId);
        ReflectionTestUtils.setField(request, "usedStampAmount", usedStampAmount);

        when(myStampRepository.findStampAmountByUserIdAndStoreId(userId, storeId)).thenReturn(10);
        when(otpRepository.existsByOtpAndStoreIdAndStatus(
                        any(Long.class), eq(storeId), eq(OtpStatus.PENDING)))
                .thenReturn(false);

        // when
        OtpResponse response = otpCustomerService.issueDeomOtp(request);

        // then
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getOtpCode());
        verify(otpRedisService).saveOtpToRedis(any(Long.class), any(OtpRedisDto.class), eq(10800L));
        verify(otpRepository).save(any(OtpUsage.class));
    }

    @Test
    @DisplayName("덤 OTP 발급 실패 - 스탬프 부족")
    void issueDeomOtpFailWhenInsufficientStampTest() {
        // given
        OtpDeomRequest request = new OtpDeomRequest();
        Long userId = 1L;
        Long storeId = 1L;
        Integer usedStampAmount = 10;

        ReflectionTestUtils.setField(request, "userId", userId);
        ReflectionTestUtils.setField(request, "storeId", storeId);
        ReflectionTestUtils.setField(request, "type", OtpType.DEOM);
        ReflectionTestUtils.setField(request, "deomId", 1L);
        ReflectionTestUtils.setField(request, "usedStampAmount", usedStampAmount);

        when(myStampRepository.findStampAmountByUserIdAndStoreId(userId, storeId)).thenReturn(5);

        // when & then
        OtpException exception =
                assertThrows(OtpException.class, () -> otpCustomerService.issueDeomOtp(request));
        assertEquals(CommonErrorCode.INVALID_STAMP_USAGE, exception.getBaseResponseCode());
    }

    @Test
    @DisplayName("덤 OTP 발급 실패 - 스탬프 없음")
    void issueDeomOtpFailWhenNoStampTest() {
        // given
        OtpDeomRequest request = new OtpDeomRequest();
        Long userId = 1L;
        Long storeId = 1L;
        Integer usedStampAmount = 5;

        ReflectionTestUtils.setField(request, "userId", userId);
        ReflectionTestUtils.setField(request, "storeId", storeId);
        ReflectionTestUtils.setField(request, "type", OtpType.DEOM);
        ReflectionTestUtils.setField(request, "deomId", 1L);
        ReflectionTestUtils.setField(request, "usedStampAmount", usedStampAmount);

        when(myStampRepository.findStampAmountByUserIdAndStoreId(userId, storeId)).thenReturn(null);

        // when & then
        OtpException exception =
                assertThrows(OtpException.class, () -> otpCustomerService.issueDeomOtp(request));
        assertEquals(CommonErrorCode.INVALID_STAMP_USAGE, exception.getBaseResponseCode());
    }

    @Test
    @DisplayName("OTP 코드 중복 체크 후 재생성 테스트")
    void generateUniqueOtpCodeTest() {
        // given
        OtpStampRequest request = new OtpStampRequest();
        ReflectionTestUtils.setField(request, "userId", 1L);
        ReflectionTestUtils.setField(request, "storeId", 1L);
        ReflectionTestUtils.setField(request, "type", OtpType.STAMP);

        // 첫 번째 생성 시도에서는 중복, 두 번째에서는 중복 없음
        when(otpRepository.existsByOtpAndStoreIdAndStatus(
                        any(Long.class), eq(1L), eq(OtpStatus.PENDING)))
                .thenReturn(true)
                .thenReturn(false);

        // when
        OtpResponse response = otpCustomerService.issueStampOtp(request);

        // then
        assertNotNull(response);
        assertNotNull(response.getOtpCode());
        // 최소 두 번 이상 OTP 코드 존재 여부 확인
        verify(otpRepository, atLeast(2))
                .existsByOtpAndStoreIdAndStatus(any(Long.class), eq(1L), eq(OtpStatus.PENDING));
    }

    @Test
    @DisplayName("OTP Redis 저장 테스트")
    void saveOtpToRedisTest() {
        // given
        OtpStampRequest request = new OtpStampRequest();
        ReflectionTestUtils.setField(request, "userId", 1L);
        ReflectionTestUtils.setField(request, "storeId", 1L);
        ReflectionTestUtils.setField(request, "type", OtpType.STAMP);

        when(otpRepository.existsByOtpAndStoreIdAndStatus(
                        any(Long.class), eq(1L), eq(OtpStatus.PENDING)))
                .thenReturn(false);

        // 캡처할 ArgumentCaptor 생성
        ArgumentCaptor<OtpRedisDto> otpRedisDtoCaptor = ArgumentCaptor.forClass(OtpRedisDto.class);

        // when
        OtpResponse response = otpCustomerService.issueStampOtp(request);

        // then
        verify(otpRedisService)
                .saveOtpToRedis(any(Long.class), otpRedisDtoCaptor.capture(), eq(10800L));

        OtpRedisDto capturedDto = otpRedisDtoCaptor.getValue();
        assertEquals(1L, capturedDto.getUserId());
        assertEquals(1L, capturedDto.getStoreId());
        assertEquals(OtpType.STAMP, capturedDto.getType());
        assertNull(capturedDto.getDeomId());
        assertNull(capturedDto.getUsedStampAmount());
        assertNotNull(capturedDto.getCreatedAt());
    }

    @Test
    @DisplayName("OTP 사용 내역 저장 테스트")
    void saveOtpUsageTest() {
        // given
        OtpStampRequest request = new OtpStampRequest();
        ReflectionTestUtils.setField(request, "userId", 1L);
        ReflectionTestUtils.setField(request, "storeId", 1L);
        ReflectionTestUtils.setField(request, "type", OtpType.STAMP);

        when(otpRepository.existsByOtpAndStoreIdAndStatus(
                        any(Long.class), eq(1L), eq(OtpStatus.PENDING)))
                .thenReturn(false);

        // 캡처할 ArgumentCaptor 생성
        ArgumentCaptor<OtpUsage> otpUsageCaptor = ArgumentCaptor.forClass(OtpUsage.class);

        // when
        OtpResponse response = otpCustomerService.issueStampOtp(request);

        // then
        verify(otpRepository).save(otpUsageCaptor.capture());

        OtpUsage capturedUsage = otpUsageCaptor.getValue();
        assertEquals(1L, capturedUsage.getUserId());
        assertEquals(1L, capturedUsage.getStoreId());
        assertEquals(OtpType.STAMP, capturedUsage.getType());
        assertEquals(OtpStatus.PENDING, capturedUsage.getStatus());
        assertNotNull(capturedUsage.getOtp());
    }

    @Test
    @DisplayName("OTP 코드 범위 테스트 (1000-9999)")
    void otpCodeRangeTest() {
        // given
        OtpStampRequest request = new OtpStampRequest();
        ReflectionTestUtils.setField(request, "userId", 1L);
        ReflectionTestUtils.setField(request, "storeId", 1L);
        ReflectionTestUtils.setField(request, "type", OtpType.STAMP);

        when(otpRepository.existsByOtpAndStoreIdAndStatus(
                        any(Long.class), eq(1L), eq(OtpStatus.PENDING)))
                .thenReturn(false);

        // when
        OtpResponse response = otpCustomerService.issueStampOtp(request);

        // then
        Long otpCode = response.getOtpCode();
        assertTrue(otpCode >= 1000 && otpCode <= 9999, "OTP 코드는 1000에서 9999 사이여야 합니다.");
    }

    @Test
    @DisplayName("스탬프 OTP와 덤 OTP 필드 검증 테스트")
    void validateOtpFieldsTest() {
        // given
        // 스탬프 OTP 요청
        OtpStampRequest stampRequest = new OtpStampRequest();
        ReflectionTestUtils.setField(stampRequest, "userId", 1L);
        ReflectionTestUtils.setField(stampRequest, "storeId", 1L);
        ReflectionTestUtils.setField(stampRequest, "type", OtpType.STAMP);

        // 덤 OTP 요청
        OtpDeomRequest deomRequest = new OtpDeomRequest();
        Long userId = 1L;
        Long storeId = 1L;
        Long deomId = 1L;
        Integer usedStampAmount = 5;

        ReflectionTestUtils.setField(deomRequest, "userId", userId);
        ReflectionTestUtils.setField(deomRequest, "storeId", storeId);
        ReflectionTestUtils.setField(deomRequest, "type", OtpType.DEOM);
        ReflectionTestUtils.setField(deomRequest, "deomId", deomId);
        ReflectionTestUtils.setField(deomRequest, "usedStampAmount", usedStampAmount);

        when(myStampRepository.findStampAmountByUserIdAndStoreId(userId, storeId)).thenReturn(10);
        when(otpRepository.existsByOtpAndStoreIdAndStatus(
                        any(Long.class), eq(storeId), eq(OtpStatus.PENDING)))
                .thenReturn(false);

        ArgumentCaptor<OtpRedisDto> redisCaptor = ArgumentCaptor.forClass(OtpRedisDto.class);
        // when
        otpCustomerService.issueStampOtp(stampRequest);
        otpCustomerService.issueDeomOtp(deomRequest);
        // then
        verify(otpRedisService, times(2))
                .saveOtpToRedis(any(Long.class), any(OtpRedisDto.class), eq(10800L));

        // 하나의 캡처로 두 번의 호출을 모두 캡처
        verify(otpRedisService, times(2))
                .saveOtpToRedis(any(Long.class), redisCaptor.capture(), eq(10800L));

        // 캡처된 모든 값 가져오기
        List<OtpRedisDto> capturedDtos = redisCaptor.getAllValues();

        // 스탬프 OTP 검증 (첫 번째 호출)
        OtpRedisDto stampOtpDto = capturedDtos.get(0);
        assertEquals(OtpType.STAMP, stampOtpDto.getType());
        assertNull(stampOtpDto.getDeomId());
        assertNull(stampOtpDto.getUsedStampAmount());

        // 덤 OTP 검증 (두 번째 호출)
        OtpRedisDto deomOtpDto = capturedDtos.get(1);
        assertEquals(OtpType.DEOM, deomOtpDto.getType());
        assertEquals(deomId, deomOtpDto.getDeomId());
        assertEquals(usedStampAmount, deomOtpDto.getUsedStampAmount());
    }
}
