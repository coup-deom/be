package kr.kro.deom.domain.otp.service;

import java.util.List;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.myStamp.entity.MyStamp;
import kr.kro.deom.domain.myStamp.exception.MyStampException;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.otp.dto.response.OwnerStampInfoResponse;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.exception.OtpException;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import kr.kro.deom.domain.stampPolicy.dto.StampPolicyDto;
import kr.kro.deom.domain.stampPolicy.service.StampPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<OwnerStampInfoResponse>> getUserStampStatusAndStampPolicy(
            Long otpId) {

        OtpUsage otpUsage = validateAndGetOtpUsage(otpId);
        int customerStampAmount =
                getCustomerStampAmount(otpUsage.getUserId(), otpUsage.getStoreId());
        List<StampPolicyDto> stampPolicyList = getStoreStampPolicies(otpUsage.getStoreId());
        OwnerStampInfoResponse response =
                createStampInfoResponse(customerStampAmount, stampPolicyList);

        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    // 적립 승인
    @Transactional
    public ResponseEntity<ApiResponse<Void>> approveOtpAndAddStamp(Long otpId, int amount) {

        validateAmount(amount);
        OtpUsage otpUsage = getValidOtpUsage(otpId);
        increaseStamp(otpUsage, amount);
        otpUsage.approve();
        otpRepository.save(otpUsage);
        otpRedisService.deleteOtpFromRedis(otpId);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> rejectStampOtp(Long otpId) {
        OtpUsage otpUsage = getValidOtpUsage(otpId);
        otpUsage.reject();

        otpRepository.save(otpUsage);
        otpRedisService.deleteOtpFromRedis(otpId);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK));
    }

    private OtpUsage validateAndGetOtpUsage(Long otpId) {
        return otpRepository
                .findById(otpId)
                .orElseThrow(() -> new OtpException(CommonErrorCode.OTP_INVALID));
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

    private OtpUsage getValidOtpUsage(Long otpId) {
        OtpUsage otpUsage =
                otpRepository
                        .findById(otpId)
                        .orElseThrow(() -> new OtpException(CommonErrorCode.OTP_INVALID));

        if (otpUsage.getStatus() != OtpStatus.PENDING) {
            if (otpUsage.getStatus() == OtpStatus.EXPIRED) {
                throw new OtpException(CommonErrorCode.OPT_EXPIRED);
            }
            throw new OtpException(CommonErrorCode.OPT_ALREADY_PROCESSED);
        }
        return otpUsage;
    }

    private void increaseStamp(OtpUsage otpUsage, int amount) {
        myStampRepository
                .incrementStamp(otpUsage.getUserId(), otpUsage.getStoreId(), amount)
                .orElseGet(
                        () ->
                                myStampRepository.save(
                                        new MyStamp(
                                                otpUsage.getUserId(),
                                                otpUsage.getStoreId(),
                                                amount)));
    }
}
