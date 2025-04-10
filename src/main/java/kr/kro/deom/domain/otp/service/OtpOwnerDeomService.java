package kr.kro.deom.domain.otp.service;

import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.otp.dto.request.DeomUsageRequestDto;
import kr.kro.deom.domain.otp.entity.DeomUsage;
import kr.kro.deom.domain.otp.entity.TransactionStatus;
import kr.kro.deom.domain.otp.exception.OtpException;
import kr.kro.deom.domain.otp.repository.DeomUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpOwnerDeomService {
    private final OtpOwnerService otpOwnerService;
    private final DeomUsageRepository deomUsageRepository;
    private final MyStampRepository myStampRepository;

    // 덤 정책
    //    public ResponseEntity<ApiResponse<OwnerStampInfoResponse>>
    // getUserStampStatusAndDeomPolicy(
    //            Long otpCode, Long storeId) {
    //
    //        OtpRedisDto otpUsage = otpRedisService.getOtpFromRedis(otpCode, storeId);
    //        int customerStampAmount =
    //                getCustomerStampAmount(otpUsage.getUserId(), otpUsage.getStoreId());
    //        List<StampPolicyDto> stampPolicyList = getStoreStampPolicies(otpUsage.getStoreId());
    //        OwnerStampInfoResponse response =
    //                createStampInfoResponse(customerStampAmount, stampPolicyList);
    //
    //        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    //    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> approveOtp(DeomUsageRequestDto deomUsageRequestDto) {
        Long customerId = deomUsageRequestDto.getUserId();
        Long storeId = deomUsageRequestDto.getStoreId();
        Long otpCode = deomUsageRequestDto.getOtpCode();
        Integer usedStampAmount = deomUsageRequestDto.getUsedStampAmount();
        // TODO: 서비스 참조로 변경
        Integer stampAmount =
                myStampRepository.findStampAmountByUserIdAndStoreId(customerId, storeId);
        if (stampAmount == null || stampAmount < usedStampAmount) {
            rejectOtp(deomUsageRequestDto); // 괜찮은코드인가...
            throw new OtpException(CommonErrorCode.OTP_INVALID, "사용할 수 있는 스탬프가 부족합니다.");
        }
        otpOwnerService.approveOtp(otpCode, customerId, storeId);
        DeomUsage deomUsage =
                createDeomUsage(customerId, storeId, usedStampAmount, TransactionStatus.APPROVED);
        deomUsageRepository.save(deomUsage);
        // 스탬프 사용량 업데이트 -> 서비스 레이어 없이 직접 repository 호출 괜찮은가...
        // TODO: 서비스 참조로 변경
        myStampRepository.updateStampAmount(customerId, storeId, usedStampAmount);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> rejectOtp(DeomUsageRequestDto deomUsageRequestDto) {
        Long customerId = deomUsageRequestDto.getUserId();
        Long storeId = deomUsageRequestDto.getStoreId();
        Long otpCode = deomUsageRequestDto.getOtpCode();
        Integer usedStampAmount = deomUsageRequestDto.getUsedStampAmount();

        otpOwnerService.rejectOtp(otpCode, customerId, storeId);
        DeomUsage deomUsage =
                createDeomUsage(customerId, storeId, usedStampAmount, TransactionStatus.REJECTED);
        deomUsageRepository.save(deomUsage);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK));
    }

    private DeomUsage createDeomUsage(
            Long customerId, Long storeId, Integer usedStampAmount, TransactionStatus status) {
        return DeomUsage.builder()
                .userId(customerId)
                .storeId(storeId)
                .usedStampAmount(usedStampAmount)
                .status(status)
                .build();
    }
}
