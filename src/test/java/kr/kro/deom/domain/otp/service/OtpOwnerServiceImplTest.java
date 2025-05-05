package kr.kro.deom.domain.otp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.Instant;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpType;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.exception.OtpException;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OtpOwnerServiceImplTest {

    @Mock private OtpRepository otpRepository;

    @Mock private OtpRedisService otpRedisService;

    @InjectMocks private OtpOwnerServiceImpl otpOwnerService;

    private Long otpCode;
    private Long userId;
    private Long storeId;

    @BeforeEach
    void setUp() {
        otpCode = 1234L;
        userId = 100L;
        storeId = 200L;
    }

    @Nested
    @DisplayName("approveOtp 메소드 테스트")
    class ApproveOtpTest {

        @Test
        @DisplayName("유효한 OTP 승인 처리가 성공적으로 이루어져야 한다")
        void approveValidOtp() {
            // given
            OtpUsage mockOtpUsage = mock(OtpUsage.class);
            when(mockOtpUsage.getUserId()).thenReturn(userId);

            when(otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING))
                    .thenReturn(mockOtpUsage);

            // when
            otpOwnerService.approveOtp(otpCode, userId, storeId);

            // then
            verify(mockOtpUsage).setStatus(OtpStatus.APPROVED);
            verify(otpRepository).save(mockOtpUsage);
            verify(otpRedisService).deleteOtpFromRedis(otpCode, storeId);
        }

        @Test
        @DisplayName("OTP가 존재하지 않을 때 예외가 발생해야 한다")
        void throwExceptionWhenOtpNotFound() {
            // given
            when(otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING))
                    .thenReturn(null);

            // when & then
            OtpException exception =
                    assertThrows(
                            OtpException.class,
                            () -> {
                                otpOwnerService.approveOtp(otpCode, userId, storeId);
                            });

            assertEquals(CommonErrorCode.OTP_UNAUTHORIZED, exception.getBaseResponseCode());
            verify(otpRepository, never()).save(any(OtpUsage.class));
            verify(otpRedisService, never()).deleteOtpFromRedis(anyLong(), anyLong());
        }

        @Test
        @DisplayName("사용자 ID가 일치하지 않을 때 예외가 발생해야 한다")
        void throwExceptionWhenUserIdMismatch() {
            // given
            Long differentUserId = 999L;
            OtpUsage mockOtpUsage = mock(OtpUsage.class);
            when(mockOtpUsage.getUserId()).thenReturn(userId);

            when(otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING))
                    .thenReturn(mockOtpUsage);

            // when & then
            OtpException exception =
                    assertThrows(
                            OtpException.class,
                            () -> {
                                otpOwnerService.approveOtp(otpCode, differentUserId, storeId);
                            });

            assertEquals(CommonErrorCode.OTP_UNAUTHORIZED, exception.getBaseResponseCode());
            verify(otpRepository, never()).save(any(OtpUsage.class));
            verify(otpRedisService, never()).deleteOtpFromRedis(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("rejectOtp 메소드 테스트")
    class RejectOtpTest {

        @Test
        @DisplayName("유효한 OTP 거절 처리가 성공적으로 이루어져야 한다")
        void rejectValidOtp() {
            // given
            OtpUsage mockOtpUsage = mock(OtpUsage.class);
            when(mockOtpUsage.getUserId()).thenReturn(userId);

            when(otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING))
                    .thenReturn(mockOtpUsage);

            // when
            otpOwnerService.rejectOtp(otpCode, userId, storeId);

            // then
            verify(mockOtpUsage).setStatus(OtpStatus.REJECTED);
            verify(otpRepository).save(mockOtpUsage);
            verify(otpRedisService).deleteOtpFromRedis(otpCode, storeId);
        }

        @Test
        @DisplayName("OTP가 존재하지 않을 때 예외가 발생해야 한다")
        void throwExceptionWhenOtpNotFound() {
            // given
            when(otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING))
                    .thenReturn(null);

            // when & then
            OtpException exception =
                    assertThrows(
                            OtpException.class,
                            () -> {
                                otpOwnerService.rejectOtp(otpCode, userId, storeId);
                            });

            assertEquals(CommonErrorCode.OTP_UNAUTHORIZED, exception.getBaseResponseCode());
            verify(otpRepository, never()).save(any(OtpUsage.class));
            verify(otpRedisService, never()).deleteOtpFromRedis(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("verifyOtp 메소드 테스트")
    class VerifyOtpTest {

        @Test
        @DisplayName("Redis에서 OTP 정보를 성공적으로 조회해야 한다")
        void getOtpFromRedisSuccess() {
            // given
            OtpRedisDto mockOtpRedisDto = mock(OtpRedisDto.class);
            when(mockOtpRedisDto.getStoreId()).thenReturn(storeId);

            when(otpRedisService.getOtpFromRedis(otpCode, storeId)).thenReturn(mockOtpRedisDto);

            // when
            OtpRedisDto result = otpOwnerService.verifyOtp(otpCode, storeId);

            // then
            assertNotNull(result);
            assertEquals(mockOtpRedisDto, result);
            verify(otpRedisService).getOtpFromRedis(otpCode, storeId);
            verify(otpRepository, never())
                    .findByOtpAndStoreIdAndStatus(anyLong(), anyLong(), any());
        }

        @Test
        @DisplayName("Redis에서 OTP 정보를 찾을 수 없을 때 DB에서 조회해야 한다")
        void getOtpFromDatabaseWhenRedisFailure() {
            // given
            when(otpRedisService.getOtpFromRedis(otpCode, storeId)).thenReturn(null);

            // OtpUsage 객체 모킹
            OtpUsage mockOtpUsage = mock(OtpUsage.class);
            when(mockOtpUsage.getUserId()).thenReturn(userId);
            when(mockOtpUsage.getStoreId()).thenReturn(storeId);
            when(mockOtpUsage.getType()).thenReturn(OtpType.DEOM);
            when(mockOtpUsage.getDeomId()).thenReturn(300L);
            when(mockOtpUsage.getUsedStampAmount()).thenReturn(5);
            Instant now = Instant.now();
            when(mockOtpUsage.getCreatedAt()).thenReturn(now);

            when(otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING))
                    .thenReturn(mockOtpUsage);

            // when
            OtpRedisDto result = otpOwnerService.verifyOtp(otpCode, storeId);

            // then
            assertNotNull(result);
            assertEquals(userId, result.getUserId());
            assertEquals(storeId, result.getStoreId());
            assertEquals(OtpType.DEOM, result.getType());
            assertEquals(300L, result.getDeomId());
            assertEquals(5, result.getUsedStampAmount());
            assertEquals(now, result.getCreatedAt());

            // 메서드 호출 검증
            verify(otpRedisService).getOtpFromRedis(otpCode, storeId);
            verify(otpRepository).findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);
        }

        @Test
        @DisplayName("Redis와 DB 모두에서 OTP 정보를 찾을 수 없을 때 예외가 발생해야 한다")
        void throwExceptionWhenOtpNotFoundAnywhere() {
            // given
            when(otpRedisService.getOtpFromRedis(otpCode, storeId)).thenReturn(null);
            when(otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING))
                    .thenReturn(null);

            // when & then
            OtpException exception =
                    assertThrows(
                            OtpException.class,
                            () -> {
                                otpOwnerService.verifyOtp(otpCode, storeId);
                            });

            assertEquals(CommonErrorCode.OTP_INVALID, exception.getBaseResponseCode());
        }

        @Test
        @DisplayName("Redis에서 찾은 OTP의 storeId가 일치하지 않을 때 예외가 발생해야 한다")
        void throwExceptionWhenStoreIdMismatch() {
            // given
            Long differentStoreId = 999L;
            OtpRedisDto mockOtpRedisDto = mock(OtpRedisDto.class);
            when(mockOtpRedisDto.getStoreId()).thenReturn(differentStoreId);

            when(otpRedisService.getOtpFromRedis(otpCode, storeId)).thenReturn(mockOtpRedisDto);

            // when & then
            OtpException exception =
                    assertThrows(
                            OtpException.class,
                            () -> {
                                otpOwnerService.verifyOtp(otpCode, storeId);
                            });

            assertEquals(CommonErrorCode.OTP_UNAUTHORIZED, exception.getBaseResponseCode());
        }
    }
}
