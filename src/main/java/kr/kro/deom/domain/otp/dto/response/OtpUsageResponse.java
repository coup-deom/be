package kr.kro.deom.domain.otp.dto.response;

import java.time.Instant;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpType;
import lombok.Builder;

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
