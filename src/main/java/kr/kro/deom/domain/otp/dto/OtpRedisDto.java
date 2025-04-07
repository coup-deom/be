package kr.kro.deom.domain.otp.dto;

import java.time.Instant;
import kr.kro.deom.domain.otp.entity.OtpType;
import lombok.*;

@Getter
@Setter
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
}
