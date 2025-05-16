package kr.kro.deom.domain.exchange.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.stream.Collectors;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.exchange.dto.*;
import kr.kro.deom.domain.exchange.entity.StampExchange;
import kr.kro.deom.domain.exchange.exception.StampExchangeException;
import kr.kro.deom.domain.exchange.repository.StampExchangeRepository;
import kr.kro.deom.domain.myStamp.entity.MyStamp;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.myStamp.service.MyStampService;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StampExchangeServiceTest1 {

    @Mock private StampExchangeRepository stampExchangeRepository;
    @Mock private StoreService storeService;
    @Mock private MyStampService myStampService;
    @Mock private MyStampRepository myStampRepository;

    @InjectMocks private StampExchangeService stampExchangeService;

    private final Long CURRENT_USER_ID = 1L;
    private final Long SOURCE_STORE_ID = 100L;
    private final Long TARGET_STORE_ID = 200L;
    private final Long EXCHANGE_ID = 300L;
    private final Integer SOURCE_AMOUNT = 10;
    private final Integer TARGET_AMOUNT = 5;

    private Store sourceStore;
    private Store targetStore;
    private StampExchange testExchange;

    @BeforeEach
    void setUp() {
        sourceStore = createMockStore(SOURCE_STORE_ID, "Source Store", "Branch 1");
        targetStore = createMockStore(TARGET_STORE_ID, "Target Store", "Branch 2");

        testExchange =
                StampExchange.builder()
                        .id(EXCHANGE_ID)
                        .creatorId(CURRENT_USER_ID)
                        .sourceStoreId(SOURCE_STORE_ID)
                        .targetStoreId(TARGET_STORE_ID)
                        .sourceAmount(SOURCE_AMOUNT)
                        .targetAmount(TARGET_AMOUNT)
                        .status(StampExchange.Status.PENDING)
                        .build();
    }

    private Store createMockStore(Long id, String name, String branch) {
        Store store = mock(Store.class, withSettings().lenient());
        when(store.getStoreName()).thenReturn(name);
        when(store.getBranchName()).thenReturn(branch);
        when(store.getId()).thenReturn(id);
        return store;
    }

    private StampExchangeJoinProjection createMockProjection(
            Long sourceStoreId, Long targetStoreId, Integer sourceAmount) {
        StampExchange exchange =
                StampExchange.builder()
                        .id((long) (Math.random() * 1000))
                        .creatorId(CURRENT_USER_ID)
                        .sourceStoreId(sourceStoreId)
                        .targetStoreId(targetStoreId)
                        .sourceAmount(sourceAmount)
                        .targetAmount(TARGET_AMOUNT)
                        .status(StampExchange.Status.PENDING)
                        .build();

        Store source =
                createMockStore(sourceStoreId, "Store " + sourceStoreId, "Branch " + sourceStoreId);
        Store target =
                createMockStore(targetStoreId, "Store " + targetStoreId, "Branch " + targetStoreId);

        StampExchangeResponse response = StampExchangeResponse.from(exchange, source, target);

        StampExchangeJoinProjection projection =
                mock(StampExchangeJoinProjection.class, withSettings().lenient());
        when(projection.toResponse()).thenReturn(response);
        return projection;
    }

    private List<StampExchangeJoinProjection> createMockProjections(int count) {
        List<StampExchangeJoinProjection> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(
                    createMockProjection(SOURCE_STORE_ID + i, TARGET_STORE_ID + i, SOURCE_AMOUNT));
        }
        return result;
    }

    private MyStamp createMyStamp(Long userId, Long storeId, Integer amount) {
        MyStamp stamp = mock(MyStamp.class);
        when(stamp.getUserId()).thenReturn(userId);
        when(stamp.getStoreId()).thenReturn(storeId);
        when(stamp.getStampAmount()).thenReturn(amount);
        return stamp;
    }

    private StampExchange safeExchange(StampExchange input) {
        if (input == null) {
            return StampExchange.builder()
                    .id(999L)
                    .creatorId(CURRENT_USER_ID)
                    .sourceStoreId(SOURCE_STORE_ID)
                    .targetStoreId(TARGET_STORE_ID)
                    .sourceAmount(1)
                    .targetAmount(1)
                    .status(StampExchange.Status.PENDING)
                    .build();
        }
        return input;
    }

    private StampExchange createMockExchange(
            Long creatorId,
            Long sourceId,
            Long targetId,
            Integer sourceAmount,
            Integer targetAmount) {
        return StampExchange.builder()
                .id((long) (Math.random() * 1000))
                .creatorId(creatorId)
                .sourceStoreId(sourceId)
                .targetStoreId(targetId)
                .sourceAmount(sourceAmount)
                .targetAmount(targetAmount)
                .status(StampExchange.Status.PENDING)
                .build();
    }

    @Test
    @DisplayName("스탬프 교환 생성 성공 테스트")
    void createStampExchange_Success() {
        // Given
        StampExchangeRequest request = new StampExchangeRequest();
        request.setCreatorId(CURRENT_USER_ID);
        request.setSourceStoreId(SOURCE_STORE_ID);
        request.setTargetStoreId(TARGET_STORE_ID);
        request.setSourceAmount(SOURCE_AMOUNT);
        request.setTargetAmount(TARGET_AMOUNT);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(myStampService.getMyStampAmount(CURRENT_USER_ID, SOURCE_STORE_ID))
                    .thenReturn(20); // 더 많은 스탬프 보유
            when(storeService.getStore(SOURCE_STORE_ID)).thenReturn(sourceStore);
            when(storeService.getStore(TARGET_STORE_ID)).thenReturn(targetStore);
            when(stampExchangeRepository.save(any(StampExchange.class)))
                    .thenAnswer(
                            invocation -> {
                                StampExchange saved = invocation.getArgument(0);
                                // 실제로는 ID가 생성되어야 함
                                return saved;
                            });

            // When
            StampExchangeResponse response = stampExchangeService.createStampExchange(request);

            // Then
            assertNotNull(response);
            assertEquals(SOURCE_STORE_ID, response.sourceStoreId());
            assertEquals(TARGET_STORE_ID, response.targetStoreId());
            assertEquals(SOURCE_AMOUNT, response.sourceAmount());
            assertEquals(TARGET_AMOUNT, response.targetAmount());
            assertEquals(CURRENT_USER_ID, response.creatorId());
            assertEquals("Source Store", response.sourceStoreName());
            assertEquals("Target Store", response.targetStoreName());

            verify(stampExchangeRepository).save(any(StampExchange.class));
        }
    }

    @Test
    @DisplayName("잔액 부족으로 스탬프 교환 생성 실패 테스트")
    void createStampExchange_InsufficientStamps() {
        // Given
        StampExchangeRequest request = new StampExchangeRequest();
        request.setCreatorId(CURRENT_USER_ID);
        request.setSourceStoreId(SOURCE_STORE_ID);
        request.setTargetStoreId(TARGET_STORE_ID);
        request.setSourceAmount(SOURCE_AMOUNT);
        request.setTargetAmount(TARGET_AMOUNT);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(myStampService.getMyStampAmount(CURRENT_USER_ID, SOURCE_STORE_ID))
                    .thenReturn(5); // 스탬프 부족

            // When/Then
            StampExchangeException exception =
                    assertThrows(
                            StampExchangeException.class,
                            () -> stampExchangeService.createStampExchange(request));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.INSUFFICIENT_STAMP_AMOUNT);
            verify(stampExchangeRepository, never()).save(any(StampExchange.class));
        }
    }

    @Test
    @DisplayName("스탬프 없는 경우 교환 생성 실패 테스트")
    void createStampExchange_NoStamps() {
        // Given
        StampExchangeRequest request = new StampExchangeRequest();
        request.setCreatorId(CURRENT_USER_ID);
        request.setSourceStoreId(SOURCE_STORE_ID);
        request.setTargetStoreId(TARGET_STORE_ID);
        request.setSourceAmount(SOURCE_AMOUNT);
        request.setTargetAmount(TARGET_AMOUNT);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(myStampService.getMyStampAmount(CURRENT_USER_ID, SOURCE_STORE_ID))
                    .thenReturn(null); // 스탬프 없음

            // When/Then
            StampExchangeException exception =
                    assertThrows(
                            StampExchangeException.class,
                            () -> stampExchangeService.createStampExchange(request));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.MY_STAMP_NOT_FOUND);
            verify(stampExchangeRepository, never()).save(any(StampExchange.class));
        }
    }

    @Test
    @DisplayName("스탬프 교환 업데이트 성공 테스트")
    void updateStampExchange_Success() {
        // Given
        StampExchangeUpdateRequest request = new StampExchangeUpdateRequest();
        request.setSourceStoreId(SOURCE_STORE_ID);
        request.setTargetStoreId(TARGET_STORE_ID);
        request.setSourceAmount(15); // 변경된 금액
        request.setTargetAmount(8); // 변경된 금액

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(myStampService.getMyStampAmount(CURRENT_USER_ID, SOURCE_STORE_ID))
                    .thenReturn(20); // 충분한 스탬프
            when(stampExchangeRepository.findById(EXCHANGE_ID))
                    .thenReturn(Optional.of(testExchange));
            when(storeService.getStore(SOURCE_STORE_ID)).thenReturn(sourceStore);
            when(storeService.getStore(TARGET_STORE_ID)).thenReturn(targetStore);

            // When
            StampExchangeResponse response =
                    stampExchangeService.updateStampExchange(EXCHANGE_ID, request);

            // Then
            assertNotNull(response);
            assertEquals(SOURCE_STORE_ID, response.sourceStoreId());
            assertEquals(TARGET_STORE_ID, response.targetStoreId());
            assertEquals(15, testExchange.getSourceAmount()); // 변경된 값 확인
            assertEquals("Source Store", response.sourceStoreName());
            assertEquals("Target Store", response.targetStoreName());
        }
    }

    @Test
    @DisplayName("존재하지 않는 교환 업데이트 실패 테스트")
    void updateStampExchange_ExchangeNotFound() {
        // Given
        StampExchangeUpdateRequest request = new StampExchangeUpdateRequest();
        request.setSourceStoreId(SOURCE_STORE_ID);
        request.setTargetStoreId(TARGET_STORE_ID);
        request.setSourceAmount(15);
        request.setTargetAmount(8);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(myStampService.getMyStampAmount(CURRENT_USER_ID, SOURCE_STORE_ID)).thenReturn(20);
            when(stampExchangeRepository.findById(EXCHANGE_ID))
                    .thenReturn(Optional.empty()); // 교환 없음

            // When/Then
            StampExchangeException exception =
                    assertThrows(
                            StampExchangeException.class,
                            () -> stampExchangeService.updateStampExchange(EXCHANGE_ID, request));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.STAMP_EXCHANGE_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("스탬프 교환 삭제 성공 테스트")
    void deleteStampExchange_Success() {
        // Given
        when(stampExchangeRepository.existsById(EXCHANGE_ID)).thenReturn(true);
        doNothing().when(stampExchangeRepository).deleteById(EXCHANGE_ID);

        // When
        assertDoesNotThrow(() -> stampExchangeService.deleteStampExchange(EXCHANGE_ID));

        // Then
        verify(stampExchangeRepository).deleteById(EXCHANGE_ID);
    }

    @Test
    @DisplayName("존재하지 않는 교환 삭제 실패 테스트")
    void deleteStampExchange_ExchangeNotFound() {
        // Given
        when(stampExchangeRepository.existsById(EXCHANGE_ID)).thenReturn(false);

        // When/Then
        StampExchangeException exception =
                assertThrows(
                        StampExchangeException.class,
                        () -> stampExchangeService.deleteStampExchange(EXCHANGE_ID));

        assertThat(exception.getBaseResponseCode())
                .isEqualTo(CommonErrorCode.STAMP_EXCHANGE_NOT_FOUND);
        verify(stampExchangeRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("모든 교환 조회 테스트")
    void getAllExchanges_Success() {
        // Given
        List<StampExchangeJoinProjection> projections = createMockProjections(3);
        when(stampExchangeRepository.findAllWithStoreInfo()).thenReturn(projections);

        // When
        List<StampExchangeResponse> responses = stampExchangeService.getAllExchanges();

        // Then
        assertEquals(3, responses.size());
        verify(stampExchangeRepository).findAllWithStoreInfo();
    }

    @Test
    @DisplayName("내 스토어 교환 조회 테스트")
    void getMyStoreExchanges_Success() {
        // Given
        List<Long> myStoreIds = List.of(SOURCE_STORE_ID, 101L, 102L);
        List<StampExchangeJoinProjection> projections = createMockProjections(2);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(myStampService.getMyStoreIds(CURRENT_USER_ID)).thenReturn(myStoreIds);
            when(stampExchangeRepository.findBySourceStoreIdInWithStoreInfo(myStoreIds))
                    .thenReturn(projections);

            // When
            List<StampExchangeResponse> responses = stampExchangeService.getMyStoreExchanges();

            // Then
            assertEquals(2, responses.size());
            verify(stampExchangeRepository).findBySourceStoreIdInWithStoreInfo(myStoreIds);
        }
    }

    @Test
    @DisplayName("내 스토어가 없을 때 교환 조회 테스트")
    void getMyStoreExchanges_NoStores() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(myStampService.getMyStoreIds(CURRENT_USER_ID)).thenReturn(Collections.emptyList());

            // When
            List<StampExchangeResponse> responses = stampExchangeService.getMyStoreExchanges();

            // Then
            assertTrue(responses.isEmpty());
            verify(stampExchangeRepository, never()).findBySourceStoreIdInWithStoreInfo(anyList());
        }
    }

    @Test
    @DisplayName("교환 가능한 스탬프 조회 테스트")
    void getTradableExchanges_Success() {
        // Given
        List<MyStamp> userStamps =
                List.of(
                        createMyStamp(CURRENT_USER_ID, SOURCE_STORE_ID, 20),
                        createMyStamp(CURRENT_USER_ID, 101L, 15));

        List<Long> storeIds =
                userStamps.stream().map(MyStamp::getStoreId).collect(Collectors.toList());

        List<StampExchangeJoinProjection> projections =
                List.of(
                        createMockProjection(SOURCE_STORE_ID, TARGET_STORE_ID, 10), // 가능 (20 > 10)
                        createMockProjection(101L, TARGET_STORE_ID, 20) // 불가능 (15 < 20)
                        );

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(myStampRepository.findAllByUserIdWithStamps(CURRENT_USER_ID))
                    .thenReturn(userStamps);
            when(stampExchangeRepository.findBySourceStoreIdInWithStoreInfo(storeIds))
                    .thenReturn(projections);

            // When
            List<StampExchangeResponse> responses = stampExchangeService.getTradableExchanges();

            // Then
            assertEquals(1, responses.size()); // 하나만 교환 가능
        }
    }

    @Test
    @DisplayName("스탬프가 없을 때 교환 가능한 스탬프 조회 테스트")
    void getTradableExchanges_NoStamps() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(myStampRepository.findAllByUserIdWithStamps(CURRENT_USER_ID))
                    .thenReturn(Collections.emptyList());

            // When
            List<StampExchangeResponse> responses = stampExchangeService.getTradableExchanges();

            // Then
            assertTrue(responses.isEmpty());
            verify(stampExchangeRepository, never()).findBySourceStoreIdInWithStoreInfo(anyList());
        }
    }

    @Test
    @DisplayName("교환 실행 성공 테스트")
    void executeExchange_Success() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(stampExchangeRepository.findById(EXCHANGE_ID))
                    .thenReturn(Optional.of(testExchange));
            when(stampExchangeRepository.updateStatusIfPending(EXCHANGE_ID, CURRENT_USER_ID))
                    .thenReturn(1);
            when(storeService.getStore(SOURCE_STORE_ID)).thenReturn(sourceStore);
            when(storeService.getStore(TARGET_STORE_ID)).thenReturn(targetStore);

            // 스탬프 양 검증
            when(myStampService.getMyStampAmount(eq(CURRENT_USER_ID), eq(TARGET_STORE_ID)))
                    .thenReturn(TARGET_AMOUNT);
            when(myStampService.getMyStampAmount(eq(CURRENT_USER_ID), eq(SOURCE_STORE_ID)))
                    .thenReturn(SOURCE_AMOUNT);

            // 스탬프 차감 성공
            when(myStampRepository.deductStampAmountIfSufficient(
                            eq(CURRENT_USER_ID), eq(TARGET_STORE_ID), eq(TARGET_AMOUNT)))
                    .thenReturn(1);
            when(myStampRepository.deductStampAmountIfSufficient(
                            eq(testExchange.getCreatorId()),
                            eq(SOURCE_STORE_ID),
                            eq(SOURCE_AMOUNT)))
                    .thenReturn(1);

            // 스탬프 추가 (void 메서드의 경우 doNothing().when() 사용)
            doNothing()
                    .when(myStampRepository)
                    .updateStampAmount(eq(CURRENT_USER_ID), eq(SOURCE_STORE_ID), eq(SOURCE_AMOUNT));
            doNothing()
                    .when(myStampRepository)
                    .updateStampAmount(
                            eq(testExchange.getCreatorId()),
                            eq(TARGET_STORE_ID),
                            eq(TARGET_AMOUNT));

            // When
            StampExchangeExecutionResponse response =
                    stampExchangeService.executeExchange(EXCHANGE_ID);

            // Then
            assertNotNull(response);
            assertEquals(EXCHANGE_ID, response.exchangeId());
            assertEquals(SOURCE_STORE_ID, response.sourceStoreId());
            assertEquals(TARGET_STORE_ID, response.targetStoreId());
            assertEquals(SOURCE_AMOUNT, response.sourceAmount());
            assertEquals(TARGET_AMOUNT, response.targetAmount());

            // 트랜잭션 검증
            assertEquals(CURRENT_USER_ID, response.responderTransaction().userId());
            assertEquals(testExchange.getCreatorId(), response.creatorTransaction().userId());

            // 스탬프 업데이트 호출 검증
            verify(myStampRepository, times(2))
                    .deductStampAmountIfSufficient(anyLong(), anyLong(), anyInt());
            verify(myStampRepository, times(2)).updateStampAmount(anyLong(), anyLong(), anyInt());
        }
    }

    @Test
    @DisplayName("이미 완료된 교환 실행 실패 테스트")
    void executeExchange_AlreadyCompleted() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(stampExchangeRepository.findById(EXCHANGE_ID))
                    .thenReturn(Optional.of(testExchange));
            when(stampExchangeRepository.updateStatusIfPending(EXCHANGE_ID, CURRENT_USER_ID))
                    .thenReturn(0); // 이미 완료됨

            // When/Then
            StampExchangeException exception =
                    assertThrows(
                            StampExchangeException.class,
                            () -> stampExchangeService.executeExchange(EXCHANGE_ID));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.STAMP_EXCHANGE_ALREADY_COMPLETED);

            // 스탬프 업데이트 호출 안됨
            verify(myStampRepository, never())
                    .deductStampAmountIfSufficient(anyLong(), anyLong(), anyInt());
            verify(myStampRepository, never()).updateStampAmount(anyLong(), anyLong(), anyInt());
        }
    }

    @Test
    @DisplayName("교환 실행 스탬프 부족 실패 테스트")
    void executeExchange_InsufficientStamps() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(stampExchangeRepository.findById(EXCHANGE_ID))
                    .thenReturn(Optional.of(testExchange));
            when(stampExchangeRepository.updateStatusIfPending(EXCHANGE_ID, CURRENT_USER_ID))
                    .thenReturn(1);

            // 스탬프 양 검증
            when(myStampService.getMyStampAmount(eq(CURRENT_USER_ID), eq(TARGET_STORE_ID)))
                    .thenReturn(TARGET_AMOUNT);
            when(myStampService.getMyStampAmount(eq(CURRENT_USER_ID), eq(SOURCE_STORE_ID)))
                    .thenReturn(SOURCE_AMOUNT);

            // 정확한 인자값으로 stub 설정
            when(myStampRepository.deductStampAmountIfSufficient(
                            CURRENT_USER_ID, TARGET_STORE_ID, TARGET_AMOUNT))
                    .thenReturn(0);

            // 차감 실패

            // When/Then
            StampExchangeException exception =
                    assertThrows(
                            StampExchangeException.class,
                            () -> stampExchangeService.executeExchange(EXCHANGE_ID));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.INSUFFICIENT_STAMP_AMOUNT);

            // 호출 여부 검증
            verify(myStampRepository)
                    .deductStampAmountIfSufficient(
                            eq(CURRENT_USER_ID), eq(TARGET_STORE_ID), eq(TARGET_AMOUNT));
            verify(myStampRepository, never()).updateStampAmount(anyLong(), anyLong(), anyInt());
        }
    }
}
