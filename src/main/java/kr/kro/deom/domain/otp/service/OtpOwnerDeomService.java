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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpOwnerDeomService {
    private final OtpOwnerService otpOwnerService;
    private final DeomUsageRepository deomUsageRepository;
    private final MyStampRepository myStampRepository;

    @Transactional
    public ResponseEntity<ApiResponse<Void>> approveOtp(DeomUsageRequestDto deomUsageRequestDto) {
        Long customerId = deomUsageRequestDto.getUserId();
        Long storeId = deomUsageRequestDto.getStoreId();
        Long otpCode = deomUsageRequestDto.getOtpCode();
        Integer usedStampAmount = deomUsageRequestDto.getUsedStampAmount();
        // TODO: 서비스 참조로 변경
        validateStamp(customerId, storeId, usedStampAmount, otpCode);

        otpOwnerService.approveOtp(otpCode, customerId, storeId);
        DeomUsage deomUsage =
                createDeomUsage(customerId, storeId, usedStampAmount, TransactionStatus.APPROVED);

        deomUsageRepository.save(deomUsage);

        updateStampAmount(customerId, storeId, usedStampAmount);

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


    private void validateStamp(Long customerId, Long storeId, Integer usedStampAmount, Long otpCode) {

        Integer stampAmount =
                myStampRepository.findStampAmountByUserIdAndStoreId(customerId, storeId);

        if (stampAmount == null || stampAmount < usedStampAmount) {
            throw new OtpException(CommonErrorCode.INVALID_STAMP_USAGE);
        }
    }

    private void updateStampAmount(Long customerId, Long storeId, Integer usedStampAmount) {
        // TODO: 스탬프 서비스로 추출 예정
        myStampRepository.updateStampAmount(customerId, storeId, usedStampAmount);
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
