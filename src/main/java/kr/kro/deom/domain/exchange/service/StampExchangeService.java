package kr.kro.deom.domain.exchange.service;

import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.exchange.dto.StampExchangeRequest;
import kr.kro.deom.domain.exchange.dto.StampExchangeResponse;
import kr.kro.deom.domain.exchange.dto.StampExchangeUpdateRequest;
import kr.kro.deom.domain.exchange.entity.StampExchange;
import kr.kro.deom.domain.exchange.exception.StampExchangeException;
import kr.kro.deom.domain.exchange.repository.StampExchangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StampExchangeService {

    private final StampExchangeRepository stampExchangeRepository;


    @Transactional
    public void createStampExchange(StampExchangeRequest request) {
        StampExchange exchange = StampExchange.builder()
                .creatorId(request.getCreatorId())
                .sourceStoreId(request.getSourceStoreId())
                .targetStoreId(request.getTargetStoreId())
                .sourceAmount(request.getSourceAmount())
                .targetAmount(request.getTargetAmount())
                .status(StampExchange.Status.PENDING)
                .build();

        stampExchangeRepository.save(exchange);

        //return StampExchangeResponse.from(exchange);
    }

    @Transactional
    public void updateStampExchange(Long userId, StampExchangeUpdateRequest request) {
        StampExchange exchange = stampExchangeRepository.findById(userId)
                .orElseThrow(() -> new StampExchangeException(CommonErrorCode.STAMP_EXCHANGE_NOT_FOUND));

        exchange.changeAmounts(
                request.getSourceAmount(),
                request.getTargetAmount()
        );

        //return StampExchangeResponse.from(exchange);
    }

    public List<StampExchangeResponse> getAllExchanges() {
        return stampExchangeRepository.findAllWithStoreInfo().stream()
                .map(StampExchangeJoinProjection::toResponse)
                .collect(Collectors.toList());
    }




}
