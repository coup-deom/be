package kr.kro.deom.domain.exchange.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StampExchangeRequest {
    private Long creatorId;
    private Long sourceStoreId;
    private Long targetStoreId;
    private Integer sourceAmount;
    private Integer targetAmount;
}
