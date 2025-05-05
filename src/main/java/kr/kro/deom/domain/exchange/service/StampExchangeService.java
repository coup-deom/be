package kr.kro.deom.domain.exchange.service;

import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.exchange.dto.StampExchangeRequest;
import kr.kro.deom.domain.exchange.dto.StampExchangeResponse;
import kr.kro.deom.domain.exchange.dto.StampExchangeUpdateRequest;
import kr.kro.deom.domain.exchange.entity.StampExchange;
import kr.kro.deom.domain.exchange.exception.StampExchangeException;
import kr.kro.deom.domain.exchange.repository.StampExchangeRepository;
import kr.kro.deom.domain.myStamp.service.MyStampService;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StampExchangeService {

    private final StampExchangeRepository stampExchangeRepository;
    private final StoreService storeService;
    private final MyStampService myStampService;

    @Transactional
    public StampExchangeResponse createStampExchange(StampExchangeRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        validateStampAmount(userId, request.getSourceStoreId(), request.getSourceAmount());

        StampExchange exchange = StampExchange.builder()
                .creatorId(request.getCreatorId())
                .sourceStoreId(request.getSourceStoreId())
                .targetStoreId(request.getTargetStoreId())
                .sourceAmount(request.getSourceAmount())
                .targetAmount(request.getTargetAmount())
                .status(StampExchange.Status.PENDING)
                .build();

        stampExchangeRepository.save(exchange);

        Store sourceStore = storeService.getStore(request.getSourceStoreId());
        Store targetStore = storeService.getStore(request.getTargetStoreId());

        return StampExchangeResponse.from(exchange, sourceStore, targetStore);
    }

    @Transactional
    public StampExchangeResponse updateStampExchange(Long stampExchangeId, StampExchangeUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        validateStampAmount(userId, request.getSourceStoreId(), request.getSourceAmount());

        StampExchange exchange = stampExchangeRepository.findById(stampExchangeId)
                .orElseThrow(() -> new StampExchangeException(CommonErrorCode.STAMP_EXCHANGE_NOT_FOUND));

        Store sourceStore = storeService.getStore(request.getSourceStoreId());
        Store targetStore = storeService.getStore(request.getTargetStoreId());

        exchange.updateExchangeTerms(request.getSourceStoreId(), request.getTargetStoreId(), request.getSourceAmount(), request.getSourceAmount());

        return StampExchangeResponse.from(exchange, sourceStore, targetStore);
    }

    @Transactional
    public void deleteStampExchange(Long stampExchangeId) {
        if (!stampExchangeRepository.existsById(stampExchangeId)) {
            throw new StampExchangeException(CommonErrorCode.STAMP_EXCHANGE_NOT_FOUND);
        }

        stampExchangeRepository.deleteById(stampExchangeId);
    }

    private void validateStampAmount(Long userId, Long storeId, Integer requested) {
        if (myStampService.getMyStampAmount(userId, storeId) < requested) {
            throw new StampExchangeException(CommonErrorCode.INSUFFICIENT_STAMP_AMOUNT);
        }
    }

    public List<StampExchangeResponse> getAllExchanges() {
        return stampExchangeRepository.findAllWithStoreInfo().stream()
                .map(StampExchangeJoinProjection::toResponse)
                .collect(Collectors.toList());
    }


}
