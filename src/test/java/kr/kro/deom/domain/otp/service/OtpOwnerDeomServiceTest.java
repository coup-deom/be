package kr.kro.deom.domain.otp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.otp.dto.request.DeomUsageRequestDto;
import kr.kro.deom.domain.otp.exception.OtpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OtpOwnerDeomServiceTest {

    @Mock private OtpOwnerService otpOwnerService;

    @Mock private MyStampRepository myStampRepository;

    @InjectMocks private OtpOwnerDeomService otpOwnerDeomService;

    private DeomUsageRequestDto deomUsageRequestDto;

    @BeforeEach
    void setUp() {
        deomUsageRequestDto =
                DeomUsageRequestDto.builder()
                        .userId(1L)
                        .storeId(2L)
                        .otpCode(1234L)
                        .deomId(3L)
                        .usedStampAmount(5)
                        .build();
    }

    @Nested
    @DisplayName("approveOtp 메소드 테스트")
    class ApproveOtpTest {

        @Test
        @DisplayName("스탬프 수량이 충분할 때 OTP 승인이 성공해야 한다")
        void approveOtpSuccessWhenStampAmountIsSufficient() {
            // given
            when(myStampRepository.findStampAmountByUserIdAndStoreId(anyLong(), anyLong()))
                    .thenReturn(10); // 사용자가 가진 스탬프 수량: 10

            // when
            otpOwnerDeomService.approveOtp(deomUsageRequestDto);

            // then
            verify(otpOwnerService)
                    .approveOtp(
                            deomUsageRequestDto.getOtpCode(),
                            deomUsageRequestDto.getUserId(),
                            deomUsageRequestDto.getStoreId());
            verify(myStampRepository)
                    .updateStampAmount(
                            deomUsageRequestDto.getUserId(),
                            deomUsageRequestDto.getStoreId(),
                            deomUsageRequestDto.getUsedStampAmount());
        }

        @Test
        @DisplayName("스탬프 수량이 부족할 때 OTP 승인이 실패해야 한다")
        void approveOtpFailWhenStampAmountIsInsufficient() {
            // given
            when(myStampRepository.findStampAmountByUserIdAndStoreId(anyLong(), anyLong()))
                    .thenReturn(3); // 사용자가 가진 스탬프 수량: 3 (요청한 수량: 5)

            // when & then
            OtpException exception =
                    assertThrows(
                            OtpException.class,
                            () -> {
                                otpOwnerDeomService.approveOtp(deomUsageRequestDto);
                            });

            assertEquals(CommonErrorCode.INVALID_STAMP_USAGE, exception.getBaseResponseCode());
            verify(otpOwnerService, never()).approveOtp(anyLong(), anyLong(), anyLong());
            verify(myStampRepository, never()).updateStampAmount(anyLong(), anyLong(), anyInt());
        }

        @Test
        @DisplayName("사용자의 스탬프 정보가 없을 때 OTP 승인이 실패해야 한다")
        void approveOtpFailWhenStampInfoNotFound() {
            // given
            when(myStampRepository.findStampAmountByUserIdAndStoreId(anyLong(), anyLong()))
                    .thenReturn(null); // 스탬프 정보 없음

            // when & then
            OtpException exception =
                    assertThrows(
                            OtpException.class,
                            () -> {
                                otpOwnerDeomService.approveOtp(deomUsageRequestDto);
                            });

            assertEquals(CommonErrorCode.INVALID_STAMP_USAGE, exception.getBaseResponseCode());
            verify(otpOwnerService, never()).approveOtp(anyLong(), anyLong(), anyLong());
            verify(myStampRepository, never()).updateStampAmount(anyLong(), anyLong(), anyInt());
        }
    }

    @Nested
    @DisplayName("rejectOtp 메소드 테스트")
    class RejectOtpTest {

        @Test
        @DisplayName("OTP 거절이 성공적으로 처리되어야 한다")
        void rejectOtpSuccess() {
            // when
            otpOwnerDeomService.rejectOtp(deomUsageRequestDto);

            // then
            verify(otpOwnerService)
                    .rejectOtp(
                            deomUsageRequestDto.getOtpCode(),
                            deomUsageRequestDto.getUserId(),
                            deomUsageRequestDto.getStoreId());
        }
    }

    @Nested
    @DisplayName("스탬프 검증 및 업데이트 테스트")
    class StampValidationAndUpdateTest {

        @Test
        @DisplayName("스탬프 수량 검증 예외 테스트 - 경계값 테스트")
        void stampValidationEdgeCases() {
            // 1. 요청한 스탬프 수량과 동일한 경우 (성공)
            when(myStampRepository.findStampAmountByUserIdAndStoreId(anyLong(), anyLong()))
                    .thenReturn(5); // 사용자가 가진 스탬프 수량: 5 (요청한 수량: 5)

            // should not throw
            assertDoesNotThrow(() -> otpOwnerDeomService.approveOtp(deomUsageRequestDto));

            // 2. 스탬프 수량이 0일 경우 (실패)
            when(myStampRepository.findStampAmountByUserIdAndStoreId(anyLong(), anyLong()))
                    .thenReturn(0);

            OtpException exception =
                    assertThrows(
                            OtpException.class,
                            () -> {
                                otpOwnerDeomService.approveOtp(deomUsageRequestDto);
                            });

            assertEquals(CommonErrorCode.INVALID_STAMP_USAGE, exception.getBaseResponseCode());
        }

        @Test
        @DisplayName("스탬프 수량 업데이트가 올바르게 호출되어야 한다")
        void stampAmountUpdateCorrectInvocation() {
            // given
            when(myStampRepository.findStampAmountByUserIdAndStoreId(anyLong(), anyLong()))
                    .thenReturn(10);

            // when
            otpOwnerDeomService.approveOtp(deomUsageRequestDto);

            // then - 정확한 파라미터로 updateStampAmount가 호출되었는지 검증
            verify(myStampRepository)
                    .updateStampAmount(
                            eq(deomUsageRequestDto.getUserId()),
                            eq(deomUsageRequestDto.getStoreId()),
                            eq(deomUsageRequestDto.getUsedStampAmount()));
        }
    }

    @Nested
    @DisplayName("통합 테스트 시나리오")
    class IntegrationScenarios {

        @Test
        @DisplayName("OTP 승인 후 거절시도 시나리오")
        void approveAndRejectScenario() {
            // given - 먼저 승인
            when(myStampRepository.findStampAmountByUserIdAndStoreId(anyLong(), anyLong()))
                    .thenReturn(10);

            otpOwnerDeomService.approveOtp(deomUsageRequestDto);

            // 이후 동일한 OTP로 거절시도 - otpOwnerService에서 이미 처리된 OTP 예외 발생하도록 설정
            doThrow(new OtpException(CommonErrorCode.OPT_ALREADY_PROCESSED))
                    .when(otpOwnerService)
                    .rejectOtp(
                            deomUsageRequestDto.getOtpCode(),
                            deomUsageRequestDto.getUserId(),
                            deomUsageRequestDto.getStoreId());

            // when & then
            OtpException exception =
                    assertThrows(
                            OtpException.class,
                            () -> {
                                otpOwnerDeomService.rejectOtp(deomUsageRequestDto);
                            });

            assertEquals(CommonErrorCode.OPT_ALREADY_PROCESSED, exception.getBaseResponseCode());
        }

        @Test
        @DisplayName("사용자 스탬프 소진 시나리오 - 사용 후 부족한 스탬프로 추가 요청")
        void consumeStampsScenario() {
            // given - 처음에는 충분한 스탬프
            when(myStampRepository.findStampAmountByUserIdAndStoreId(anyLong(), anyLong()))
                    .thenReturn(10) // 첫 번째 호출에서는 10 반환
                    .thenReturn(5); // 두 번째 호출에서는 5 반환 (10-5=5)

            // 첫 번째 스탬프 사용 요청
            otpOwnerDeomService.approveOtp(deomUsageRequestDto);

            // 두 번째 요청도 동일한 양을 요청
            DeomUsageRequestDto secondRequest =
                    DeomUsageRequestDto.builder()
                            .userId(1L)
                            .storeId(2L)
                            .otpCode(5678L) // 다른 OTP 코드
                            .deomId(3L)
                            .usedStampAmount(5) // 잔여 스탬프량과 동일
                            .build();

            // when - 두 번째 요청 처리
            otpOwnerDeomService.approveOtp(secondRequest);

            // then - 두 번 모두 스탬프 업데이트되어야 함
            verify(myStampRepository, times(2)).updateStampAmount(anyLong(), anyLong(), anyInt());
        }
    }
}
