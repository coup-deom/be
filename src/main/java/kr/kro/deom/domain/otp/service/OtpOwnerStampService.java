package kr.kro.deom.domain.otp.service;

import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.myStamp.entity.MyStamp;
import kr.kro.deom.domain.myStamp.exception.MyStampException;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.exception.OtpException;
import kr.kro.deom.domain.otp.repository.OtpRepository;
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

    // 적립내용 조회
    // response: 지금까지 n개 적립한 고객입니다. 우리가게 스탬프 적립 가이드  ex- 5000원 이상 스탬프 1개 이런식!

    // 적립 승인
    @Transactional
    public ResponseEntity<ApiResponse<Void>> approveOtpAndAddStamp(Long optId, int amount) {

        validateAmount(amount);
        OtpUsage otpUsage = getValidOtpUsage(optId);
        increaseStamp(otpUsage, amount);
        otpUsage.approve();
        otpRepository.save(otpUsage);
        otpRedisService.deleteOtpFromRedis(optId);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> rejectStampOtp(Long optId) {
        OtpUsage otpUsage = getValidOtpUsage(optId);
        otpUsage.reject();

        otpRepository.save(otpUsage);
        otpRedisService.deleteOtpFromRedis(optId);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK));
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new MyStampException(CommonErrorCode.INVALID_STAMP_AMOUNT);
        }
    }

    private OtpUsage getValidOtpUsage(Long optId) {
        OtpUsage otpUsage =
                otpRepository
                        .findById(optId)
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
