package kr.kro.deom.domain.otp.service;

import java.util.List;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.myStamp.entity.MyStamp;
import kr.kro.deom.domain.myStamp.exception.MyStampException;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.dto.request.OtpStampApproveRequest;
import kr.kro.deom.domain.otp.dto.response.OwnerStampInfoResponse;
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
    private final OtpOwnerService otpOwnerService;

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
    public void approveOtpAndAddStamp(OtpStampApproveRequest otpStampApproveRequest) {
        Long customerId = otpStampApproveRequest.getUserId();
        Long storeId = otpStampApproveRequest.getStoreId();
        Long otpCode = otpStampApproveRequest.getOtpCode();
        Integer amount = otpStampApproveRequest.getAmount();

        validateAmount(amount);
        otpOwnerService.approveOtp(otpCode, customerId, storeId);
        // TODO: 스탬프 적립 정보 레포에 저장
        increaseStamp(customerId, storeId, amount);
    }

    @Transactional
    public void rejectStampOtp(OtpStampApproveRequest otpStampApproveRequest) {

        Long customerId = otpStampApproveRequest.getUserId();
        Long storeId = otpStampApproveRequest.getStoreId();
        Long otpCode = otpStampApproveRequest.getOtpCode();
        Integer amount = otpStampApproveRequest.getAmount();

        otpOwnerService.rejectOtp(otpCode, customerId, storeId);
        // TODO: 스탬프 적립 정보 레포에 저장

    }

    private int getCustomerStampAmount(Long userId, Long storeId) {
        return myStampRepository.findStampAmountByUserIdAndStoreId(userId, storeId);
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

    private void increaseStamp(Long customerId, Long storeId, int amount) {
        Integer affectedRows = myStampRepository.incrementStamp(customerId, storeId, amount);

        if (affectedRows == null || affectedRows == 0) {
            myStampRepository.save(new MyStamp(customerId, storeId, amount));
        }
    }
}
