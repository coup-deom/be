package kr.kro.deom.domain.myStamp.dto;

public class UserAccumulatedStampsDto {
    private Long userId;
    private Integer accumulatedStampAmount;

    public UserAccumulatedStampsDto(Long userId, Integer accumulatedStampAmount) {
        this.userId = userId;
        this.accumulatedStampAmount = accumulatedStampAmount;
    }
}
