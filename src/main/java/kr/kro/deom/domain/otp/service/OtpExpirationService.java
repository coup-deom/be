package kr.kro.deom.domain.otp.service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpExpirationService {

    private final OtpRepository otpRepository;
    private static final long OTP_TTL_SECONDS = 10800L;

    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    @Transactional
    public void expireOldOtps() {

        Instant expirationThreshold = Instant.now().minusSeconds(OTP_TTL_SECONDS);
        List<OtpUsage> expiredOtps =
                otpRepository.findByStatusAndCreatedAtBefore(
                        OtpStatus.PENDING, expirationThreshold);

        for (OtpUsage otp : expiredOtps) {
            otp.setStatus(OtpStatus.EXPIRED);
        }

        if (!expiredOtps.isEmpty()) {
            otpRepository.saveAll(expiredOtps);
        }
    }
}
