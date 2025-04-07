package kr.kro.deom.domain.otp.service;

import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.otp.dto.DeomUsageDto;
import kr.kro.deom.domain.otp.entity.DeomUsage;
import kr.kro.deom.domain.otp.entity.TransactionStatus;
import kr.kro.deom.domain.otp.repository.DeomUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpOwnerDeomService {
    private final OtpOwnerService otpOwnerService;
    private final DeomUsageRepository deomUsageRepository;
    private final MyStampRepository myStampRepository;

    @Transactional
    public void approveOtp(DeomUsageDto deomUsageDto) {
        Long userId = deomUsageDto.getUserId();
        Long storeId = deomUsageDto.getStoreId();
        Long otpCode = deomUsageDto.getOtpCode();
        Integer usedStampAmount = deomUsageDto.getUsedStampAmount();

        Integer stampAmount = myStampRepository.findStampAmountByUserIdAndStoreId(userId, storeId);
        if (stampAmount == null || stampAmount < usedStampAmount) {
            rejectOtp(deomUsageDto); // 괜찮은코드인가...
            throw new IllegalArgumentException("사용할 수 있는 스탬프가 부족합니다.");
        }
        if (otpOwnerService.approveOtp(otpCode, userId, storeId)) {
            DeomUsage deomUsage =
                    createDeomUsage(userId, storeId, usedStampAmount, TransactionStatus.APPROVED);
            deomUsageRepository.save(deomUsage);
            // 스탬프 사용량 업데이트 -> 서비스 레이어 없이 직접 repository 호출 괜찮은가...
            myStampRepository.updateStampAmount(userId, storeId, usedStampAmount);
        }
    }

    @Transactional
    public void rejectOtp(DeomUsageDto deomUsageDto) {
        Long userId = deomUsageDto.getUserId();
        Long storeId = deomUsageDto.getStoreId();
        Long otpCode = deomUsageDto.getOtpCode();
        Integer usedStampAmount = deomUsageDto.getUsedStampAmount();
        if (otpOwnerService.rejectOtp(otpCode, userId, storeId)) {
            DeomUsage deomUsage =
                    createDeomUsage(userId, storeId, usedStampAmount, TransactionStatus.REJECTED);
            deomUsageRepository.save(deomUsage);
        }
    }

    private DeomUsage createDeomUsage(
            Long userId, Long storeId, Integer usedStampAmount, TransactionStatus status) {
        return DeomUsage.builder()
                .userId(userId)
                .storeId(storeId)
                .usedStampAmount(usedStampAmount)
                .status(status)
                .build();
    }
}
