package kr.kro.deom.domain.otp.service;

import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.exception.OtpException;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpOwnerServiceImpl implements OtpOwnerService {
    private final OtpRepository otpRepository;
    private final OtpRedisService otpRedisService;

    @Override
    public void approveOtp(Long otpCode, Long userId, Long storeId) {
        // PostgreSQL
        OtpUsage otpUsage =
                otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);

        if (otpUsage == null || !userId.equals(otpUsage.getUserId())) {
            throw new OtpException(CommonErrorCode.OTP_UNAUTHORIZED);
        }

        otpUsage.setStatus(OtpStatus.APPROVED);
        otpRepository.save(otpUsage);

        // redis
        otpRedisService.deleteOtpFromRedis(otpCode, storeId);
    }

    @Override
    public void rejectOtp(Long otpCode, Long userId, Long storeId) {
        // PostgreSQL
        OtpUsage otpUsage =
                otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);

        if (otpUsage == null || !userId.equals(otpUsage.getUserId())) {
            throw new OtpException(CommonErrorCode.OTP_UNAUTHORIZED);
        }

        otpUsage.setStatus(OtpStatus.REJECTED);
        otpRepository.save(otpUsage);

        // redis
        otpRedisService.deleteOtpFromRedis(otpCode, storeId);
    }

    /** Verify OTP - 사장님이 otp입력 받았을 때 여기 조회해서 otp정보 받으면 됩 */
    @Override
    public OtpRedisDto verifyOtp(Long otpCode, Long storeId) {
        // 처음에는 레디스에서 조회
        OtpRedisDto otpRedisDto = otpRedisService.getOtpFromRedis(otpCode, storeId);

        if (otpRedisDto != null) {
            if (storeId.equals(otpRedisDto.getStoreId())) {
                return otpRedisDto;
            }
            throw new OtpException(CommonErrorCode.OTP_UNAUTHORIZED);
        }

        // 레디스 장애로 조회 실패한 경우 postgreSQL에서 조회 후 변환 후 반환
        OtpUsage otpUsage =
                otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);

        if (otpUsage == null) {
            throw new OtpException(CommonErrorCode.OTP_INVALID);
        }

        return OtpRedisDto.convertToOtpRedisDto(otpUsage);
    }
}
