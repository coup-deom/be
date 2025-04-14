package kr.kro.deom.domain.otp.dto.response;

import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpType;
import lombok.Builder;

import java.time.Instant;

@Builder
public class OtpUsageResponse {

    private Long otpId;
    private Long storeId;
    private String storeName;
    private OtpType type;
    private Integer usedStampAmount;
    private Instant createdAt;
    private OtpStatus status;
    private Long otpCode;

    private Long deomId;
    private String deomName;
}