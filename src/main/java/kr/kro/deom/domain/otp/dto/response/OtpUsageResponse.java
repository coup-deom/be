package kr.kro.deom.domain.otp.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OtpUsageResponse {

    private Long otpId;
    private Long storeId;
    private String storeName;
    private String storeImage;
    private OtpType type;
    private Integer usedStampAmount;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ssXXX",
            timezone = "Asia/Seoul")
    private Instant createdAt;

    private OtpStatus status;
    private Long otpCode;

    private Long deomId;
    private String deomName;
}
