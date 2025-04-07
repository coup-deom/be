package kr.kro.deom.domain.otp.service;

import java.time.Instant;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpType;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpUsageService {

  private final OtpRepository otpRepository;

  public void createOtpUsage(Long otpCode, OtpRedisDto info) {

    OtpUsage otpUsage =
        OtpUsage.builder()
            .otp(otpCode)
            .userId(info.getUserId())
            .storeId(info.getStoreId())
            .type(OtpType.valueOf(info.getType()))
            .createdAt(Instant.now())
            .status(OtpStatus.PENDING)
            .build();

    otpRepository.save(otpUsage);
  }

  public boolean duplicateOtp(Long otpCode, Long storeId) {
    return otpRepository.existsByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);
  }
}
