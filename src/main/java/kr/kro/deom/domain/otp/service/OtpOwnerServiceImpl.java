package kr.kro.deom.domain.otp.service;

import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.dto.request.OtpVerifyRequest;
import kr.kro.deom.domain.otp.dto.response.OtpVerifyResponse;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpOwnerServiceImpl implements OtpOwnerService {
    private final OtpRepository otpRepository;
    private final OtpRedisService otpRedisService;

    @Override
    public boolean approveOtp(Long otpCode, Long userId, Long storeId) {
        // PostgreSQL
        OtpUsage otpUsage =
                otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);

        if (otpUsage == null || !userId.equals(otpUsage.getUserId())) {
            return false;
        }

        otpUsage.setStatus(OtpStatus.APPROVED);
        otpRepository.save(otpUsage);

        // redis
        otpRedisService.deleteOtpFromRedis(otpCode);

        return true;
    }

    @Override
    public boolean rejectOtp(Long otpCode, Long userId, Long storeId) {
        // PostgreSQL
        OtpUsage otpUsage =
                otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);

        if (otpUsage == null || !userId.equals(otpUsage.getUserId())) {
            return false;
        }

        otpUsage.setStatus(OtpStatus.REJECTED);
        otpRepository.save(otpUsage);

        // redis
        otpRedisService.deleteOtpFromRedis(otpCode);

        return true;
    }

    /** Verify OTP - 사장님이 otp입력 받았을 때 여기 조회해서 otp정보 받으면 됩 */
    @Override
    public OtpRedisDto verifyOtp(Long otpCode, Long storeId) {
        // 처음에는 레디스에서 조회
        OtpRedisDto otpRedisDto = otpRedisService.getOtpFromRedis(otpCode);

        if (otpRedisDto != null) {
            if (storeId.equals(otpRedisDto.getStoreId())) {
                return otpRedisDto;
            }
            return null;
        }

        // 레디스 장애로 조회 실패한 경우 postgreSQL에서 조회 후 변환 후 반환
        OtpUsage otpUsage =
                otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);

        if (otpUsage == null) {
            return null;
        }

        return OtpRedisDto.convertToOtpRedisDto(otpUsage);
    }
}
