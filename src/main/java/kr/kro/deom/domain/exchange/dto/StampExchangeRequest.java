package kr.kro.deom.domain.exchange.dto;

import lombok.Getter;

@Getter
public class StampExchangeRequest {
    private Long creatorId;
    private Long sourceStoreId;
    private Long targetStoreId;
    private Integer sourceAmount;
    private Integer targetAmount;
}
