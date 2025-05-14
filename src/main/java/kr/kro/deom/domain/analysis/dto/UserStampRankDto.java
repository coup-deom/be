package kr.kro.deom.domain.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStampRankDto {
    private Long userId;
    private String nickname;
    private Integer accumulatedStampAmount;
    private Integer rank;
}
