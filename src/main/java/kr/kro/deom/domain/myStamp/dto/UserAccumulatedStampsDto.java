package kr.kro.deom.domain.myStamp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccumulatedStampsDto {
    private Long userId;
    private Integer accumulatedStampAmount;
}
