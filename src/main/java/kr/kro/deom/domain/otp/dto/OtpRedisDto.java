package kr.kro.deom.domain.otp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import kr.kro.deom.domain.otp.entity.OtpType;
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

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ssXXX",
            timezone = "Asia/Seoul")
    private Instant createdAt;
}
