package kr.kro.deom.domain.exchange.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
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
import kr.kro.deom.domain.store.entity.StoreStatus;
import kr.kro.deom.domain.store.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
class StampExchangeServiceTest {

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
                        .status(StampExchangeStatus.PENDING)
                        .build();
    }

    private Store createMockStore(Long id, String name, String branch) {
        return Store.builder()
                .id(id)
                .storeName(name)
                .branchName(branch)
                .ownerId(999L) // 더미값
                .businessNumber(1234567890L)
                .addressCity("서울")
                .addressStreet("종로")
                .addressDetail("1층")
                .isDeleted(false)
                .image("image.jpg")
                .status(StoreStatus.APPROVED)
                .build();
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
                        .status(StampExchangeStatus.PENDING)
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
        // when(stamp.getUserId()).thenReturn(userId);
        // when(stamp.getStoreId()).thenReturn(storeId);
        // when(stamp.getStampAmount()).thenReturn(amount);
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
                    .status(StampExchangeStatus.PENDING)
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
                .status(StampExchangeStatus.PENDING)
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
        when(stampExchangeRepository.findPendingAllExchanges()).thenReturn(projections);

        // When
        List<StampExchangeResponse> responses = stampExchangeService.getAllExchanges();

        // Then
        assertEquals(3, responses.size());
        verify(stampExchangeRepository).findPendingAllExchanges();
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

    @MockitoSettings(strictness = Strictness.LENIENT)
    @Test
    @DisplayName("보유 수량이 충분한 교환만 필터링되어 반환되는지 확인")
    void getTradableExchanges_Success() {
        // given
        // 내가 가진 스탬프들
        MyStamp stamp1 = mock(MyStamp.class);
        when(stamp1.getUserId()).thenReturn(CURRENT_USER_ID);
        when(stamp1.getStoreId()).thenReturn(TARGET_STORE_ID);
        when(stamp1.getStampAmount()).thenReturn(10);

        MyStamp stamp2 = mock(MyStamp.class);
        when(stamp2.getUserId()).thenReturn(CURRENT_USER_ID);
        when(stamp2.getStoreId()).thenReturn(101L);
        when(stamp2.getStampAmount()).thenReturn(3);

        List<MyStamp> userStamps = List.of(stamp1, stamp2);

        StampExchange tradable = createMockExchange(999L, SOURCE_STORE_ID, TARGET_STORE_ID, 8, 5);

        StampExchange untradable = createMockExchange(999L, SOURCE_STORE_ID, 101L, 10, 7);

        StampExchangeJoinProjection proj1 = mock(StampExchangeJoinProjection.class);
        StampExchangeJoinProjection proj2 = mock(StampExchangeJoinProjection.class);

        when(proj1.getExchange()).thenReturn(tradable);
        when(proj2.getExchange()).thenReturn(untradable);

        when(proj1.toResponse())
                .thenReturn(
                        StampExchangeResponse.from(
                                tradable,
                                createMockStore(SOURCE_STORE_ID, "S1", "Branch A"),
                                createMockStore(TARGET_STORE_ID, "T1", "Branch B")));

        when(proj2.toResponse())
                .thenReturn(
                        StampExchangeResponse.from(
                                untradable,
                                createMockStore(SOURCE_STORE_ID, "S2", "Branch C"),
                                createMockStore(101L, "T2", "Branch D")));

        List<StampExchangeJoinProjection> projections = List.of(proj1, proj2);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(myStampRepository.findAllByUserIdWithStamps(CURRENT_USER_ID))
                    .thenReturn(userStamps);

            // 변경된 부분: userId 파라미터 추가
            when(stampExchangeRepository.findByTargetStoreIdInWithStoreInfo(
                            anyList(), eq(CURRENT_USER_ID)))
                    .thenReturn(projections);

            // when
            List<StampExchangeResponse> result = stampExchangeService.getTradableExchanges();

            // then
            assertEquals(1, result.size()); // 교환 가능한 것만 1개 반환
            assertEquals(SOURCE_STORE_ID, result.get(0).sourceStoreId());
            assertEquals("S1", result.get(0).sourceStoreName());

            // 추가 검증: findByTargetStoreIdInWithStoreInfo가 올바른 파라미터로 호출되었는지 확인
            verify(stampExchangeRepository)
                    .findByTargetStoreIdInWithStoreInfo(
                            argThat(
                                    storeIds ->
                                            storeIds.contains(TARGET_STORE_ID)
                                                    && storeIds.contains(101L)),
                            eq(CURRENT_USER_ID)); // userId 파라미터도 검증 추가
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
    @DisplayName("내 모든 스탬프 교환 조회 - PENDING 상태")
    void getAllMyStampExchanges_PendingStatus() {
        // Given
        ExchangeStatus status = ExchangeStatus.PENDING;
        List<StampExchangeJoinProjection> projections = createMockProjections(2);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(stampExchangeRepository.findAllPendingExchangesByUserId(CURRENT_USER_ID))
                    .thenReturn(projections);

            // When
            List<StampExchangeResponse> responses =
                    stampExchangeService.getAllMyStampExchanges(status);

            // Then
            assertEquals(2, responses.size());
            verify(stampExchangeRepository).findAllPendingExchangesByUserId(CURRENT_USER_ID);
            verify(stampExchangeRepository, never()).findAllExchangesByUserId(anyLong());
        }
    }

    @Test
    @DisplayName("내 모든 스탬프 교환 조회 - ALL 상태")
    void getAllMyStampExchanges_AllStatus() {
        // Given
        ExchangeStatus status = ExchangeStatus.ALL;
        List<StampExchangeJoinProjection> projections = createMockProjections(3);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(stampExchangeRepository.findAllExchangesByUserId(CURRENT_USER_ID))
                    .thenReturn(projections);

            // When
            List<StampExchangeResponse> responses =
                    stampExchangeService.getAllMyStampExchanges(status);

            // Then
            assertEquals(3, responses.size());
            verify(stampExchangeRepository).findAllExchangesByUserId(CURRENT_USER_ID);
            verify(stampExchangeRepository, never()).findAllPendingExchangesByUserId(anyLong());
        }
    }

    @Test
    @DisplayName("교환 실행 성공 - 새로운 로직 (기존 MyStamp 업데이트)")
    void executeExchange_Success_UpdateExistingStamp() {
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
            when(myStampService.getMyStampAmount(
                            eq(testExchange.getCreatorId()), eq(SOURCE_STORE_ID)))
                    .thenReturn(SOURCE_AMOUNT);

            // 스탬프 차감 성공
            when(myStampRepository.deductStampAmountIfSufficient(
                            eq(testExchange.getCreatorId()),
                            eq(SOURCE_STORE_ID),
                            eq(SOURCE_AMOUNT)))
                    .thenReturn(1);
            when(myStampRepository.deductStampAmountIfSufficient(
                            eq(CURRENT_USER_ID), eq(TARGET_STORE_ID), eq(TARGET_AMOUNT)))
                    .thenReturn(1);

            // 스탬프 추가 성공 (기존 MyStamp가 있는 경우)
            when(myStampRepository.addStampAmount(
                            eq(testExchange.getCreatorId()),
                            eq(TARGET_STORE_ID),
                            eq(TARGET_AMOUNT)))
                    .thenReturn(1);
            when(myStampRepository.addStampAmount(
                            eq(CURRENT_USER_ID), eq(SOURCE_STORE_ID), eq(SOURCE_AMOUNT)))
                    .thenReturn(1);

            // When
            StampExchangeExecutionResponse response =
                    stampExchangeService.executeExchange(EXCHANGE_ID);

            // Then
            assertNotNull(response);
            assertEquals(EXCHANGE_ID, response.exchangeId());
            assertEquals(SOURCE_STORE_ID, response.sourceStoreId());
            assertEquals(TARGET_STORE_ID, response.targetStoreId());

            // 차감 및 추가 메서드 호출 검증 (save는 호출되지 않아야 함)
            verify(myStampRepository, times(2))
                    .deductStampAmountIfSufficient(anyLong(), anyLong(), anyInt());
            verify(myStampRepository, times(2)).addStampAmount(anyLong(), anyLong(), anyInt());
            verify(myStampRepository, never()).save(any(MyStamp.class));
        }
    }

    @Test
    @DisplayName("교환 실행 성공 - 새로운 MyStamp 생성")
    void executeExchange_Success_CreateNewMyStamp() {
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
            when(myStampService.getMyStampAmount(
                            eq(testExchange.getCreatorId()), eq(SOURCE_STORE_ID)))
                    .thenReturn(SOURCE_AMOUNT);

            // 스탬프 차감 성공
            when(myStampRepository.deductStampAmountIfSufficient(
                            eq(testExchange.getCreatorId()),
                            eq(SOURCE_STORE_ID),
                            eq(SOURCE_AMOUNT)))
                    .thenReturn(1);
            when(myStampRepository.deductStampAmountIfSufficient(
                            eq(CURRENT_USER_ID), eq(TARGET_STORE_ID), eq(TARGET_AMOUNT)))
                    .thenReturn(1);

            // 스탬프 추가 실패 (기존 MyStamp가 없는 경우) -> 새로 생성
            when(myStampRepository.addStampAmount(
                            eq(testExchange.getCreatorId()),
                            eq(TARGET_STORE_ID),
                            eq(TARGET_AMOUNT)))
                    .thenReturn(0);
            when(myStampRepository.addStampAmount(
                            eq(CURRENT_USER_ID), eq(SOURCE_STORE_ID), eq(SOURCE_AMOUNT)))
                    .thenReturn(0);

            when(myStampRepository.save(any(MyStamp.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            StampExchangeExecutionResponse response =
                    stampExchangeService.executeExchange(EXCHANGE_ID);

            // Then
            assertNotNull(response);
            assertEquals(EXCHANGE_ID, response.exchangeId());

            // 차감, 추가, 생성 메서드 호출 검증
            verify(myStampRepository, times(2))
                    .deductStampAmountIfSufficient(anyLong(), anyLong(), anyInt());
            verify(myStampRepository, times(2)).addStampAmount(anyLong(), anyLong(), anyInt());
            verify(myStampRepository, times(2)).save(any(MyStamp.class));
        }
    }

    @Test
    @DisplayName("교환 실행 실패 - responder 스탬프 부족")
    void executeExchange_ResponderInsufficientStamps() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(stampExchangeRepository.findById(EXCHANGE_ID))
                    .thenReturn(Optional.of(testExchange));
            when(stampExchangeRepository.updateStatusIfPending(EXCHANGE_ID, CURRENT_USER_ID))
                    .thenReturn(1);

            // 스탬프 양 검증
            when(myStampService.getMyStampAmount(eq(CURRENT_USER_ID), eq(TARGET_STORE_ID)))
                    .thenReturn(TARGET_AMOUNT);
            when(myStampService.getMyStampAmount(
                            eq(testExchange.getCreatorId()), eq(SOURCE_STORE_ID)))
                    .thenReturn(SOURCE_AMOUNT);

            // creator 스탬프 차감 성공
            when(myStampRepository.deductStampAmountIfSufficient(
                            eq(testExchange.getCreatorId()),
                            eq(SOURCE_STORE_ID),
                            eq(SOURCE_AMOUNT)))
                    .thenReturn(1);

            // responder 스탬프 차감 실패
            when(myStampRepository.deductStampAmountIfSufficient(
                            eq(CURRENT_USER_ID), eq(TARGET_STORE_ID), eq(TARGET_AMOUNT)))
                    .thenReturn(0);

            // When/Then
            StampExchangeException exception =
                    assertThrows(
                            StampExchangeException.class,
                            () -> stampExchangeService.executeExchange(EXCHANGE_ID));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.INSUFFICIENT_STAMP_AMOUNT);

            // 양쪽 차감이 모두 호출되어야 함
            verify(myStampRepository, times(2))
                    .deductStampAmountIfSufficient(anyLong(), anyLong(), anyInt());

            // 스탬프 추가는 호출되지 않아야 함
            verify(myStampRepository, never()).addStampAmount(anyLong(), anyLong(), anyInt());
            verify(myStampRepository, never()).save(any(MyStamp.class));
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
    @DisplayName("교환 실행 시 creator 차감 실패 → 예외 발생, responder 차감은 호출되지 않아야 함")
    void executeExchange_InsufficientStamps() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            // given
            Long creatorId = 999L;

            StampExchange exchange =
                    StampExchange.builder()
                            .id(EXCHANGE_ID)
                            .creatorId(creatorId)
                            .sourceStoreId(SOURCE_STORE_ID)
                            .targetStoreId(TARGET_STORE_ID)
                            .sourceAmount(SOURCE_AMOUNT)
                            .targetAmount(TARGET_AMOUNT)
                            .status(StampExchangeStatus.PENDING)
                            .build();

            when(stampExchangeRepository.findById(EXCHANGE_ID)).thenReturn(Optional.of(exchange));
            when(stampExchangeRepository.updateStatusIfPending(EXCHANGE_ID, CURRENT_USER_ID))
                    .thenReturn(1);

            // 스탬프 양 검증을 통과하도록 설정
            when(myStampService.getMyStampAmount(eq(CURRENT_USER_ID), eq(TARGET_STORE_ID)))
                    .thenReturn(TARGET_AMOUNT);
            when(myStampService.getMyStampAmount(eq(creatorId), eq(SOURCE_STORE_ID)))
                    .thenReturn(SOURCE_AMOUNT);

            // creator의 소스 스토어 스탬프 차감 실패
            doReturn(0)
                    .when(myStampRepository)
                    .deductStampAmountIfSufficient(
                            eq(creatorId), eq(SOURCE_STORE_ID), eq(SOURCE_AMOUNT));

            // when/then
            StampExchangeException exception =
                    assertThrows(
                            StampExchangeException.class,
                            () -> stampExchangeService.executeExchange(EXCHANGE_ID));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.INSUFFICIENT_STAMP_AMOUNT);

            // 검증
            verify(myStampRepository)
                    .deductStampAmountIfSufficient(
                            eq(creatorId), eq(SOURCE_STORE_ID), eq(SOURCE_AMOUNT));

            // responder의 스탬프 차감이 호출되지 않았는지 확인
            verify(myStampRepository, never())
                    .deductStampAmountIfSufficient(
                            eq(CURRENT_USER_ID), eq(TARGET_STORE_ID), eq(TARGET_AMOUNT));

            // 스탬프 업데이트도 호출되지 않았어야 함
            verify(myStampRepository, never()).updateStampAmount(any(), any(), any());
        }
    }
}
