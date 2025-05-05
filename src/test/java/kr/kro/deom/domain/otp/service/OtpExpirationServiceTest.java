package kr.kro.deom.domain.otp.service;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OtpExpirationServiceTest {

    @Mock private OtpRepository otpRepository;

    @InjectMocks private OtpExpirationService otpExpirationService;

    @Captor private ArgumentCaptor<List<OtpUsage>> otpListCaptor;

    @Test
    @DisplayName("만료된 OTP를 업데이트해야 한다")
    void expireOldOtps_shouldUpdateExpiredOtps() {
        // given
        OtpUsage expiredOtp1 = mock(OtpUsage.class);
        OtpUsage expiredOtp2 = mock(OtpUsage.class);
        List<OtpUsage> expiredOtps = Arrays.asList(expiredOtp1, expiredOtp2);

        when(otpRepository.findByStatusAndCreatedAtBefore(
                        eq(OtpStatus.PENDING), any(Instant.class)))
                .thenReturn(expiredOtps);

        // when
        otpExpirationService.expireOldOtps();

        // then
        verify(expiredOtp1).setStatus(OtpStatus.EXPIRED);
        verify(expiredOtp2).setStatus(OtpStatus.EXPIRED);
        verify(otpRepository).saveAll(expiredOtps);
    }

    @Test
    @DisplayName("만료된 OTP가 없을 때 업데이트하지 않아야 한다")
    void expireOldOtps_shouldNotUpdateWhenNoExpiredOtps() {
        // given
        when(otpRepository.findByStatusAndCreatedAtBefore(
                        eq(OtpStatus.PENDING), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        // when
        otpExpirationService.expireOldOtps();

        // then
        verify(otpRepository, never()).saveAll(anyList());
    }
}
