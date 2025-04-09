package kr.kro.deom.domain.otp.dto;

import java.time.Instant;
import kr.kro.deom.domain.otp.entity.OtpType;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpRedisDto {
    private Long userId;
    private Long storeId;
    private OtpType type; // "stamp" 또는 "deom"
    private Long deomId; // null 가능
    private Integer usedStampAmount;
    private Instant createdAt;

    public static OtpRedisDto convertToOtpRedisDto(OtpUsage otpUsage) {
        return OtpRedisDto.builder()
                .userId(otpUsage.getUserId())
                .storeId(otpUsage.getStoreId())
                .type(otpUsage.getType())
                .deomId(otpUsage.getDeomId())
                .usedStampAmount(otpUsage.getUsedStampAmount())
                .createdAt(otpUsage.getCreatedAt())
                .build();
    }
}
