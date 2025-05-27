package kr.kro.deom.domain.exchange.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.exchange.dto.*;
import kr.kro.deom.domain.exchange.entity.StampExchange;
import kr.kro.deom.domain.exchange.entity.StampExchangeStatus;
import kr.kro.deom.domain.exchange.exception.StampExchangeException;
import kr.kro.deom.domain.exchange.repository.StampExchangeRepository;
import kr.kro.deom.domain.myStamp.entity.MyStamp;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.myStamp.service.MyStampService;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StampExchangeService {

    private final StampExchangeRepository stampExchangeRepository;
    private final StoreService storeService;
    private final MyStampService myStampService;
    private final MyStampRepository myStampRepository;

    @Transactional(readOnly = true)
    public List<StampExchangeResponse> getAllMyStampExchanges(ExchangeStatus status) {
        Long userId = SecurityUtils.getCurrentUserId();

        if (status == ExchangeStatus.PENDING) {
            return stampExchangeRepository.findAllPendingExchangesByUserId(userId).stream()
                    .map(StampExchangeJoinProjection::toResponse)
                    .collect(Collectors.toList());

        } else if (status == ExchangeStatus.ALL) {
            return stampExchangeRepository.findAllExchangesByUserId(userId).stream()
                    .map(StampExchangeJoinProjection::toResponse)
                    .collect(Collectors.toList());
        } else {
            throw new StampExchangeException(CommonErrorCode.INVALID_STAMP_EXCHANGE_STATUS);
        }
    }

    @Transactional
    public StampExchangeResponse createStampExchange(StampExchangeRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long sourceStoreId = request.getSourceStoreId();
        Integer sourceAmount = request.getSourceAmount();
        Long targetStoreId = request.getTargetStoreId();
        Integer targetAmount = request.getTargetAmount();
        Long creatorId = request.getCreatorId();

        validateStampAmount(userId, sourceStoreId, sourceAmount);

        StampExchange exchange =
                StampExchange.builder()
                        .creatorId(creatorId)
                        .sourceStoreId(sourceStoreId)
                        .targetStoreId(targetStoreId)
                        .sourceAmount(sourceAmount)
                        .targetAmount(targetAmount)
                        .status(StampExchangeStatus.PENDING)
                        .build();

        stampExchangeRepository.save(exchange);

        Store sourceStore = storeService.getStore(sourceStoreId);
        Store targetStore = storeService.getStore(targetStoreId);

        return StampExchangeResponse.from(exchange, sourceStore, targetStore);
    }

    @Transactional
    public StampExchangeResponse updateStampExchange(
            Long stampExchangeId, StampExchangeUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long sourceStoreId = request.getSourceStoreId();
        Long targetStoreId = request.getTargetStoreId();
        Integer sourceAmount = request.getSourceAmount();
        Integer targetAmount = request.getTargetAmount();
        validateStampAmount(userId, sourceStoreId, sourceAmount);

        StampExchange exchange =
                stampExchangeRepository
                        .findById(stampExchangeId)
                        .orElseThrow(
                                () ->
                                        new StampExchangeException(
                                                CommonErrorCode.STAMP_EXCHANGE_NOT_FOUND));

        Store sourceStore = storeService.getStore(sourceStoreId);
        Store targetStore = storeService.getStore(targetStoreId);

        exchange.updateExchangeTerms(sourceStoreId, targetStoreId, sourceAmount, targetAmount);

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

        Integer stampAmount = myStampService.getMyStampAmount(userId, storeId);

        if (stampAmount == null) {
            throw new StampExchangeException(CommonErrorCode.MY_STAMP_NOT_FOUND);
        }

        if (stampAmount < requested) {
            throw new StampExchangeException(CommonErrorCode.INSUFFICIENT_STAMP_AMOUNT);
        }
    }

    public List<StampExchangeResponse> getAllExchanges() {
        return stampExchangeRepository.findPendingAllExchanges().stream()
                .map(StampExchangeJoinProjection::toResponse)
                .collect(Collectors.toList());
    }

    public List<StampExchangeResponse> getMyStoreExchanges() {

        Long userId = SecurityUtils.getCurrentUserId();

        List<Long> myStoreIds = myStampService.getMyStoreIds(userId);

        if (myStoreIds.isEmpty()) {
            return Collections.emptyList();
        }

        return stampExchangeRepository.findBySourceStoreIdInWithStoreInfo(myStoreIds).stream()
                .map(StampExchangeJoinProjection::toResponse)
                .collect(Collectors.toList());
    }

    public List<StampExchangeResponse> getTradableExchanges() {

        Long userId = SecurityUtils.getCurrentUserId();
        List<MyStamp> userStamps = myStampRepository.findAllByUserIdWithStamps(userId);

        if (userStamps.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> myStoreIds =
                userStamps.stream().map(MyStamp::getStoreId).collect(Collectors.toList());

        Map<Long, Integer> storeStampAmountMap =
                userStamps.stream()
                        .collect(Collectors.toMap(MyStamp::getStoreId, MyStamp::getStampAmount));

        List<StampExchangeJoinProjection> allExchanges =
                stampExchangeRepository.findBySourceStoreIdInWithStoreInfo(myStoreIds);

        return allExchanges.stream()
                .filter(
                        projection -> {
                            StampExchange exchange = projection.getExchange();
                            Integer userStampAmount =
                                    storeStampAmountMap.getOrDefault(
                                            exchange.getSourceStoreId(), 0);
                            return userStampAmount >= exchange.getSourceAmount();
                        })
                .map(StampExchangeJoinProjection::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StampExchangeExecutionResponse executeExchange(Long exchangeId) {
        Long userId = SecurityUtils.getCurrentUserId();

        StampExchange exchange = getExchangeOrThrow(exchangeId);

        completeExchangeOrThrow(exchangeId, userId);

        validateStampAmountBeforeExchange(exchange, userId);

        performStampExchange(exchange, userId);

        Store sourceStore = storeService.getStore(exchange.getSourceStoreId());
        Store targetStore = storeService.getStore(exchange.getTargetStoreId());

        return StampExchangeExecutionResponse.from(
                exchange, sourceStore, targetStore, exchange.getCreatorId(), userId);
    }

    private StampExchange getExchangeOrThrow(Long exchangeId) {
        return stampExchangeRepository
                .findById(exchangeId)
                .orElseThrow(
                        () -> new StampExchangeException(CommonErrorCode.STAMP_EXCHANGE_NOT_FOUND));
    }

    private void validateStampAmountBeforeExchange(StampExchange exchange, Long responderId) {
        validateStampAmount(responderId, exchange.getTargetStoreId(), exchange.getTargetAmount());
        validateStampAmount(
                exchange.getCreatorId(), exchange.getSourceStoreId(), exchange.getSourceAmount());
    }

    private void completeExchangeOrThrow(Long exchangeId, Long userId) {
        int updated = stampExchangeRepository.updateStatusIfPending(exchangeId, userId);
        if (updated == 0) {
            throw new StampExchangeException(CommonErrorCode.STAMP_EXCHANGE_ALREADY_COMPLETED);
        }
    }

    private void performStampExchange(StampExchange exchange, Long responderId) {
        Long creatorId = exchange.getCreatorId();
        Long targetStoreId = exchange.getTargetStoreId();
        Long sourceStoreId = exchange.getSourceStoreId();
        Integer sourceAmount = exchange.getSourceAmount();
        Integer targetAmount = exchange.getTargetAmount();

        deductOrThrow(creatorId, sourceStoreId, sourceAmount);
        deductOrThrow(responderId, targetStoreId, targetAmount);

        int updated1 = myStampRepository.addStampAmount(creatorId, targetStoreId, targetAmount);
        if (updated1 == 0) {
            MyStamp myStamp = new MyStamp(creatorId, targetStoreId, targetAmount);
            myStampRepository.save(myStamp);
        }
        int updated2 = myStampRepository.addStampAmount(responderId, sourceStoreId, sourceAmount);
        if (updated2 == 0) {
            MyStamp myStamp = new MyStamp(responderId, sourceStoreId, sourceAmount);
            myStampRepository.save(myStamp);

        }
    }

    private void deductOrThrow(Long userId, Long storeId, int amount) {
        int updated = myStampRepository.deductStampAmountIfSufficient(userId, storeId, amount);
        if (updated == 0) {
            throw new StampExchangeException(CommonErrorCode.INSUFFICIENT_STAMP_AMOUNT);
        }
    }
}
