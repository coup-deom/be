package kr.kro.deom.domain.exchange.dto;

import lombok.Getter;

@Getter
public class StampExchangeUpdateRequest {
    private Integer sourceAmount;
    private Integer targetAmount;
    // 가게 이름 추가
}
