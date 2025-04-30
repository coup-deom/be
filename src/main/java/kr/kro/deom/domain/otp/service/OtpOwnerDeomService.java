package kr.kro.deom.domain.otp.service;

import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.otp.dto.request.DeomUsageRequestDto;
import kr.kro.deom.domain.otp.exception.OtpException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpOwnerDeomService {
    private final OtpOwnerService otpOwnerService;
    private final MyStampRepository myStampRepository;

    @Transactional
    public void approveOtp(DeomUsageRequestDto deomUsageRequestDto) {
        Long customerId = deomUsageRequestDto.getUserId();
        Long storeId = deomUsageRequestDto.getStoreId();
        Long otpCode = deomUsageRequestDto.getOtpCode();
        Integer usedStampAmount = deomUsageRequestDto.getUsedStampAmount();
        // TODO: 서비스 참조로 변경
        validateStamp(customerId, storeId, usedStampAmount, otpCode);

        otpOwnerService.approveOtp(otpCode, customerId, storeId);

        updateStampAmount(customerId, storeId, usedStampAmount);
    }

    @Transactional
    public void rejectOtp(DeomUsageRequestDto deomUsageRequestDto) {
        Long customerId = deomUsageRequestDto.getUserId();
        Long storeId = deomUsageRequestDto.getStoreId();
        Long otpCode = deomUsageRequestDto.getOtpCode();

        otpOwnerService.rejectOtp(otpCode, customerId, storeId);
    }

    private void validateStamp(
            Long customerId, Long storeId, Integer usedStampAmount, Long otpCode) {

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
}
