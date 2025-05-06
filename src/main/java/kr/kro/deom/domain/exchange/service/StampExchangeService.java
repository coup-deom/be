package kr.kro.deom.domain.exchange.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.exchange.dto.StampExchangeRequest;
import kr.kro.deom.domain.exchange.dto.StampExchangeResponse;
import kr.kro.deom.domain.exchange.dto.StampExchangeUpdateRequest;
import kr.kro.deom.domain.exchange.entity.StampExchange;
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

    @Transactional
    public StampExchangeResponse createStampExchange(StampExchangeRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        validateStampAmount(userId, request.getSourceStoreId(), request.getSourceAmount());

        StampExchange exchange =
                StampExchange.builder()
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
    public StampExchangeResponse updateStampExchange(
            Long stampExchangeId, StampExchangeUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        validateStampAmount(userId, request.getSourceStoreId(), request.getSourceAmount());

        StampExchange exchange =
                stampExchangeRepository
                        .findById(stampExchangeId)
                        .orElseThrow(
                                () ->
                                        new StampExchangeException(
                                                CommonErrorCode.STAMP_EXCHANGE_NOT_FOUND));

        Store sourceStore = storeService.getStore(request.getSourceStoreId());
        Store targetStore = storeService.getStore(request.getTargetStoreId());

        exchange.updateExchangeTerms(
                request.getSourceStoreId(),
                request.getTargetStoreId(),
                request.getSourceAmount(),
                request.getSourceAmount());

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

    // 내가 스탬프를 보유한 가게만 조회하는 메서드
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

    // 거래가능한 가게만 조회하는 메서드 (새로 추가)
    public List<StampExchangeResponse> getTradableExchanges() {

        Long userId = SecurityUtils.getCurrentUserId();
        // 사용자의 모든 스탬프 정보 조회 (가게별 스탬프 수량)
        List<MyStamp> userStamps = myStampRepository.findAllByUserIdWithStamps(userId);

        if (userStamps.isEmpty()) {
            return Collections.emptyList();
        }

        // 사용자가 스탬프를 보유한 가게 ID 목록
        List<Long> myStoreIds =
                userStamps.stream().map(MyStamp::getStoreId).collect(Collectors.toList());

        // 가게별 보유 스탬프 수량 맵 생성
        Map<Long, Integer> storeStampAmountMap =
                userStamps.stream()
                        .collect(Collectors.toMap(MyStamp::getStoreId, MyStamp::getStampAmount));

        // 사용자가 스탬프를 보유한 가게 관련 교환 조회
        List<StampExchangeJoinProjection> allExchanges =
                stampExchangeRepository.findBySourceStoreIdInWithStoreInfo(myStoreIds);

        // 거래 가능한 교환만 필터링 (보유 스탬프 수량 >= 필요 스탬프 수량)
        return allExchanges.stream()
                .filter(
                        projection -> {
                            StampExchange exchange = projection.getExchange();
                            Integer userStampAmount =
                                    storeStampAmountMap.getOrDefault(
                                            exchange.getSourceStoreId(), 0);
                            return userStampAmount
                                    >= exchange.getSourceAmount(); // 보유 스탬프가 필요 스탬프보다 많거나 같은 경우만
                        })
                .map(StampExchangeJoinProjection::toResponse)
                .collect(Collectors.toList());
    }
}
