package kr.kro.deom.domain.analysis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import kr.kro.deom.domain.exchange.dto.StampExchangeResponse;
import kr.kro.deom.domain.exchange.entity.StampExchange;
import kr.kro.deom.domain.exchange.entity.StampExchangeStatus;
import kr.kro.deom.domain.exchange.repository.StampExchangeRepository;
import kr.kro.deom.domain.exchange.service.StampExchangeJoinProjection;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.entity.StoreStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StampExchangeAnalysisServiceTest {

    @Mock private StampExchangeRepository stampExchangeRepository;

    @InjectMocks private CustomerAnalysisService customerAnalysisService;

    private Store sourceStore;
    private Store targetStore;
    private StampExchange exchange;
    private Instant now = Instant.now();
    private Long storeId = 1L;

    @BeforeEach
    void setUp() {

        sourceStore =
                Store.builder()
                        .id(1L)
                        .ownerId(101L)
                        .businessNumber(1234567890L)
                        .storeName("Source Store")
                        .branchName("Main Branch")
                        .addressCity("Seoul")
                        .addressStreet("Gangnam Street")
                        .addressDetail("Building 123")
                        .isDeleted(false)
                        .image("source_store.jpg")
                        .status(StoreStatus.APPROVED)
                        .build();

        targetStore =
                Store.builder()
                        .id(2L)
                        .ownerId(102L)
                        .businessNumber(9876543210L)
                        .storeName("Target Store")
                        .branchName("Sub Branch")
                        .addressCity("Busan")
                        .addressStreet("Haeundae Street")
                        .addressDetail("Tower 456")
                        .isDeleted(false)
                        .image("target_store.jpg")
                        .status(StoreStatus.APPROVED)
                        .build();

        exchange =
                StampExchange.builder()
                        .id(1L)
                        .creatorId(101L)
                        .responderId(102L)
                        .sourceStoreId(sourceStore.getId())
                        .targetStoreId(targetStore.getId())
                        .sourceAmount(10)
                        .targetAmount(5)
                        .status(StampExchangeStatus.PENDING)
                        .build();
    }

    @Test
    @DisplayName("findMyStoreExchange 메소드는 해당 스토어와 관련된 모든 유효한 교환 기록을 반환해야 한다")
    void findMyStoreExchange_ShouldReturnAllValidExchangesForStore() {
        // Given
        StampExchangeJoinProjection projection = mock(StampExchangeJoinProjection.class);
        StampExchangeResponse expectedResponse =
                new StampExchangeResponse(
                        exchange.getId(),
                        now,
                        exchange.getCreatorId(),
                        exchange.getResponderId(),
                        sourceStore.getId(),
                        sourceStore.getStoreName(),
                        sourceStore.getBranchName(),
                        targetStore.getId(),
                        targetStore.getStoreName(),
                        targetStore.getBranchName(),
                        exchange.getSourceAmount(),
                        exchange.getTargetAmount(),
                        exchange.getStatus());

        when(projection.toResponse()).thenReturn(expectedResponse);
        List<StampExchangeJoinProjection> mockProjections = Arrays.asList(projection);

        when(stampExchangeRepository.findAllValidExchanges(storeId)).thenReturn(mockProjections);

        // When
        List<StampExchangeResponse> result = customerAnalysisService.findMyStoreExchange(storeId);

        // Then
        assertEquals(1, result.size());
        StampExchangeResponse actualResponse = result.get(0);

        // 기본 필드 검증
        assertEquals(expectedResponse.id(), actualResponse.id());
        assertEquals(expectedResponse.updatedAt(), actualResponse.updatedAt());
        assertEquals(expectedResponse.status(), actualResponse.status());

        // 스토어 관련 필드 검증
        assertEquals(expectedResponse.sourceStoreId(), actualResponse.sourceStoreId());
        assertEquals(expectedResponse.sourceStoreName(), actualResponse.sourceStoreName());
        assertEquals(expectedResponse.sourceBranchName(), actualResponse.sourceBranchName());
        assertEquals(expectedResponse.targetStoreId(), actualResponse.targetStoreId());
        assertEquals(expectedResponse.targetStoreName(), actualResponse.targetStoreName());
        assertEquals(expectedResponse.targetBranchName(), actualResponse.targetBranchName());

        // 교환 관련 필드 검증
        assertEquals(expectedResponse.creatorId(), actualResponse.creatorId());
        assertEquals(expectedResponse.responderId(), actualResponse.responderId());
        assertEquals(expectedResponse.sourceAmount(), actualResponse.sourceAmount());
        assertEquals(expectedResponse.targetAmount(), actualResponse.targetAmount());

        verify(stampExchangeRepository, times(1)).findAllValidExchanges(storeId);
    }

    @Test
    @DisplayName("findMyStoreExchange 메소드는 교환 기록이 없을 때 빈 리스트를 반환해야 한다")
    void findMyStoreExchange_ShouldReturnEmptyListWhenNoExchanges() {
        // Given
        when(stampExchangeRepository.findAllValidExchanges(storeId)).thenReturn(List.of());

        // When
        List<StampExchangeResponse> result = customerAnalysisService.findMyStoreExchange(storeId);

        // Then
        assertEquals(0, result.size());
        verify(stampExchangeRepository, times(1)).findAllValidExchanges(storeId);
    }

    @Test
    @DisplayName("findMyStoreExchange 메소드는 여러 개의 교환 기록을 올바르게 처리해야 한다")
    void findMyStoreExchange_ShouldHandleMultipleExchanges() {
        // Given
        Store anotherStore =
                Store.builder()
                        .id(3L)
                        .ownerId(103L)
                        .businessNumber(5555555555L)
                        .storeName("Another Store")
                        .branchName("Another Branch")
                        .addressCity("Incheon")
                        .addressStreet("Airport Street")
                        .addressDetail("Airport Plaza")
                        .isDeleted(false)
                        .image("another_store.jpg")
                        .status(StoreStatus.APPROVED)
                        .build();

        StampExchange anotherExchange =
                StampExchange.builder()
                        .id(2L)
                        .creatorId(101L)
                        .responderId(103L)
                        .sourceStoreId(storeId)
                        .targetStoreId(anotherStore.getId())
                        .sourceAmount(15)
                        .targetAmount(8)
                        .status(StampExchangeStatus.COMPLETED)
                        .build();

        StampExchangeJoinProjection projection1 = mock(StampExchangeJoinProjection.class);
        StampExchangeResponse response1 =
                new StampExchangeResponse(
                        exchange.getId(),
                        now,
                        exchange.getCreatorId(),
                        exchange.getResponderId(),
                        sourceStore.getId(),
                        sourceStore.getStoreName(),
                        sourceStore.getBranchName(),
                        targetStore.getId(),
                        targetStore.getStoreName(),
                        targetStore.getBranchName(),
                        exchange.getSourceAmount(),
                        exchange.getTargetAmount(),
                        exchange.getStatus());
        when(projection1.toResponse()).thenReturn(response1);

        StampExchangeJoinProjection projection2 = mock(StampExchangeJoinProjection.class);
        StampExchangeResponse response2 =
                new StampExchangeResponse(
                        anotherExchange.getId(),
                        now,
                        anotherExchange.getCreatorId(),
                        anotherExchange.getResponderId(),
                        sourceStore.getId(),
                        sourceStore.getStoreName(),
                        sourceStore.getBranchName(),
                        anotherStore.getId(),
                        anotherStore.getStoreName(),
                        anotherStore.getBranchName(),
                        anotherExchange.getSourceAmount(),
                        anotherExchange.getTargetAmount(),
                        anotherExchange.getStatus());
        when(projection2.toResponse()).thenReturn(response2);

        List<StampExchangeJoinProjection> mockProjections = Arrays.asList(projection1, projection2);
        when(stampExchangeRepository.findAllValidExchanges(storeId)).thenReturn(mockProjections);

        // When
        List<StampExchangeResponse> result = customerAnalysisService.findMyStoreExchange(storeId);

        // Then
        assertEquals(2, result.size());

        // 첫 번째 결과 검증
        StampExchangeResponse actualFirstResponse = result.get(0);
        assertEquals(response1.id(), actualFirstResponse.id());
        assertEquals(response1.status(), actualFirstResponse.status());

        // 두 번째 결과 검증
        StampExchangeResponse actualSecondResponse = result.get(1);
        assertEquals(response2.id(), actualSecondResponse.id());
        assertEquals(response2.status(), actualSecondResponse.status());

        verify(stampExchangeRepository, times(1)).findAllValidExchanges(storeId);
    }
}
