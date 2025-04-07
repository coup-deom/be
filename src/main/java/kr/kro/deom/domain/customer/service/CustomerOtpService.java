package kr.kro.deom.domain.customer.service;

import java.time.Instant;
import java.util.Random;
import kr.kro.deom.domain.customer.dto.OtpDeomRequest;
import kr.kro.deom.domain.customer.dto.OtpResponse;
import kr.kro.deom.domain.customer.dto.OtpStampRequest;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.entity.OtpType;
import kr.kro.deom.domain.otp.service.OtpRedisService;
import kr.kro.deom.domain.otp.service.OtpUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerOtpService {

    private final OtpUsageService otpUsageService;
    private final OtpRedisService otpRedisService;

    private static final Random RANDOM = new Random();
    private static final Long OTP_TTL_SECONDS = 10800L;

    public OtpResponse issueStampOtp(OtpStampRequest request) {
        OtpRedisDto otpInfo =
                createOtpInfo(
                        request.getUserId(), request.getStoreId(), request.getType(), null, null);
        return issueOtp(otpInfo);
    }

    public OtpResponse issueDeomOtp(OtpDeomRequest request) {
        OtpRedisDto otpInfo =
                createOtpInfo(
                        request.getUserId(),
                        request.getStoreId(),
                        request.getType(),
                        request.getDeomId(),
                        request.getUsedStampAmount());
        return issueOtp(otpInfo);
    }

    private OtpRedisDto createOtpInfo(
            Long userId, Long storeId, OtpType type, Long deomId, String usedStampAmount) {
        return OtpRedisDto.builder()
                .userId(userId)
                .storeId(storeId)
                .type(type.name())
                .deomId(deomId)
                .usedStampAmount(usedStampAmount)
                .createdAt(Instant.now().toString())
                .build();
    }

    private Long generateOtpCode(Long storeId) {
        while (true) {
            Long otpCode = 1000L + RANDOM.nextInt(9000);
            if (!otpUsageService.duplicateOtp(otpCode, storeId)) {
                return otpCode;
            }
        }
    }

    private OtpResponse issueOtp(OtpRedisDto otpInfo) {
        Long otpCode = generateOtpCode(otpInfo.getStoreId());

        otpRedisService.saveOtpToRedis(otpCode, otpInfo, OTP_TTL_SECONDS);

        otpUsageService.createOtpUsage(otpCode, otpInfo);

        return new OtpResponse(otpCode);
    }
}
