package kr.kro.deom.domain.otp.service;

import java.util.List;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.myStamp.entity.MyStamp;
import kr.kro.deom.domain.myStamp.exception.MyStampException;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.dto.response.OwnerStampInfoResponse;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.exception.OtpException;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import kr.kro.deom.domain.stampPolicy.dto.StampPolicyDto;
import kr.kro.deom.domain.stampPolicy.service.StampPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OtpOwnerStampService {

    private final OtpRepository otpRepository;
    private final MyStampRepository myStampRepository;
    private final OtpRedisService otpRedisService;
    private final StampPolicyService stampPolicyService;

    // 적립 페이지
    @Transactional(readOnly = true)
    public OwnerStampInfoResponse getUserStampStatusAndStampPolicy(Long otpCode, Long storeId) {

        OtpRedisDto otpUsage = otpRedisService.getOtpFromRedis(otpCode, storeId);
        int customerStampAmount =
                getCustomerStampAmount(otpUsage.getUserId(), otpUsage.getStoreId());
        List<StampPolicyDto> stampPolicyList = stampPolicyService.getStampPolicy(storeId);
        OwnerStampInfoResponse response =
                createStampInfoResponse(customerStampAmount, stampPolicyList);

        return response;
    }

    // 적립 승인
    @Transactional
    public void approveOtpAndAddStamp(Long otpCode, Long storeId, int amount) {

        validateAmount(amount);
        OtpUsage otpUsage = findPendingOtp(otpCode, storeId);
        increaseStamp(otpUsage.getUserId(), otpUsage.getStoreId(), amount);
        otpUsage.approve();
        otpRepository.save(otpUsage);
        otpRedisService.deleteOtpFromRedis(otpCode, storeId);
    }

    @Transactional
    public void rejectStampOtp(Long otpCode, Long storeId) {

        OtpUsage otpUsage = findPendingOtp(otpCode, storeId);
        otpUsage.reject();
        otpRepository.save(otpUsage);
        otpRedisService.deleteOtpFromRedis(otpCode, storeId);
    }

    private int getCustomerStampAmount(Long userId, Long storeId) {
        return myStampRepository.findStampAmountByUserIdAndStoreId(userId, storeId);
    }

    private List<StampPolicyDto> getStoreStampPolicies(Long storeId) {
        return stampPolicyService.getStampPolicy(storeId);
    }

    private OwnerStampInfoResponse createStampInfoResponse(
            int customerStampAmount, List<StampPolicyDto> stampPolicyList) {
        return OwnerStampInfoResponse.builder()
                .customerStampAmount(customerStampAmount)
                .stampPolicyList(stampPolicyList)
                .build();
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new MyStampException(CommonErrorCode.INVALID_STAMP_AMOUNT);
        }
    }

    private void increaseStamp(Long userId, Long storeId, int amount) {
        Integer affectedRows =
                myStampRepository.incrementStamp(
                        userId, storeId, amount);

        if (affectedRows == null || affectedRows == 0) {
            myStampRepository.save(
                    new MyStamp(userId,storeId, amount));
        }
    }

    private OtpUsage findPendingOtp(Long otpCode, Long storeId) {
        OtpUsage otpUsage =
                otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);
        if (otpUsage == null) {
            throw new OtpException(CommonErrorCode.OTP_INVALID);
        } else if (!otpUsage.getStoreId().equals(storeId)) {
            throw new OtpException(CommonErrorCode.OTP_UNAUTHORIZED);
        }
        return otpUsage;
    }
}
