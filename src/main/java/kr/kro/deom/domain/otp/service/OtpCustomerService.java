package kr.kro.deom.domain.otp.service;

import kr.kro.deom.domain.otp.dto.OtpDeomRequest;
import kr.kro.deom.domain.otp.dto.OtpResponse;
import kr.kro.deom.domain.otp.dto.OtpStampRequest;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpType;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpCustomerService {

    private final OtpRedisService otpRedisService;
    private final OtpRepository otpRepository;

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
            Long userId, Long storeId, OtpType type, Long deomId, Integer usedStampAmount) {
        return OtpRedisDto.builder()
                .userId(userId)
                .storeId(storeId)
                .type(type)
                .deomId(deomId)
                .usedStampAmount(usedStampAmount)
                .createdAt(Instant.now())
                .build();
    }

    private Long generateOtpCode(Long storeId) {
        while (true) {
            Long otpCode = 1000L + RANDOM.nextInt(9000);
            if (!otpRepository.existsByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING)) {
                return otpCode;
            }
        }
    }

    private OtpResponse issueOtp(OtpRedisDto otpInfo) {
        Long otpCode = generateOtpCode(otpInfo.getStoreId());

        otpRedisService.saveOtpToRedis(otpCode, otpInfo, OTP_TTL_SECONDS);

        createOtpUsage(otpCode, otpInfo);

        return new OtpResponse(otpCode);
    }

    private void createOtpUsage(Long otpCode, OtpRedisDto info) {

        OtpUsage otpUsage =
                OtpUsage.builder()
                        .otp(otpCode)
                        .userId(info.getUserId())
                        .storeId(info.getStoreId())
                        .type(info.getType())
                        .createdAt(Instant.now())
                        .status(OtpStatus.PENDING)
                        .build();

        otpRepository.save(otpUsage);
    }

}
