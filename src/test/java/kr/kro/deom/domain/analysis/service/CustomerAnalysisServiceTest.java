package kr.kro.deom.domain.analysis.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import kr.kro.deom.domain.analysis.dto.UserStampRankDto;
import kr.kro.deom.domain.myStamp.dto.UserAccumulatedStampsDto;
import kr.kro.deom.domain.myStamp.service.MyStampService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerAnalysisServiceTest {

    @Mock private MyStampService myStampService;

    @InjectMocks private CustomerAnalysisService customerAnalysisService;

    @Test
    @DisplayName("고객 누적 스탬프 순위 조회")
    void getCustomerRankingByAccumulatedStamp() {
        // given
        Long storeId = 1L;
        List<UserAccumulatedStampsDto> userAccumulatedStamps =
                Arrays.asList(
                        new UserAccumulatedStampsDto(1L, 100), // 1등
                        new UserAccumulatedStampsDto(2L, 80), // 2등
                        new UserAccumulatedStampsDto(3L, 50) // 3등
                        );

        when(myStampService.getUserAccumulatedStamps(storeId)).thenReturn(userAccumulatedStamps);

        // when
        List<UserStampRankDto> result =
                customerAnalysisService.getCustomerRankingByAccumulatedStamp(storeId);

        // then
        assertEquals(3, result.size());

        // 1등 검증
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(100, result.get(0).getAccumulatedStampAmount());
        assertEquals(1, result.get(0).getRank());

        // 2등 검증
        assertEquals(2L, result.get(1).getUserId());
        assertEquals(80, result.get(1).getAccumulatedStampAmount());
        assertEquals(2, result.get(1).getRank());

        // 3등 검증
        assertEquals(3L, result.get(2).getUserId());
        assertEquals(50, result.get(2).getAccumulatedStampAmount());
        assertEquals(3, result.get(2).getRank());

        verify(myStampService, times(1)).getUserAccumulatedStamps(storeId);
    }

    @Test
    @DisplayName("고객 누적 스탬프 순위 조회 - 동점 처리")
    void getCustomerRankingByAccumulatedStamp_withTiedRanks() {
        // given
        Long storeId = 1L;
        List<UserAccumulatedStampsDto> userAccumulatedStamps =
                Arrays.asList(
                        new UserAccumulatedStampsDto(1L, 100), // 1등
                        new UserAccumulatedStampsDto(2L, 80), // 2등
                        new UserAccumulatedStampsDto(3L, 80), // 2등 (동점)
                        new UserAccumulatedStampsDto(4L, 50), // 4등
                        new UserAccumulatedStampsDto(5L, 30), // 5등
                        new UserAccumulatedStampsDto(6L, 20), // 6등
                        new UserAccumulatedStampsDto(7L, 20), // 6등 (동점)
                        new UserAccumulatedStampsDto(8L, 20), // 6등 (동점)
                        new UserAccumulatedStampsDto(9L, 10) // 9등
                        );

        when(myStampService.getUserAccumulatedStamps(storeId)).thenReturn(userAccumulatedStamps);

        // when
        List<UserStampRankDto> result =
                customerAnalysisService.getCustomerRankingByAccumulatedStamp(storeId);

        // then
        assertEquals(9, result.size());

        assertAll(
                () -> assertEquals(1, result.get(0).getRank()), // 100 pts
                () -> assertEquals(2, result.get(1).getRank()), // 80 pts
                () -> assertEquals(2, result.get(2).getRank()), // 80 pts
                () -> assertEquals(4, result.get(3).getRank()), // 50 pts
                () -> assertEquals(5, result.get(4).getRank()), // 30 pts
                () -> assertEquals(6, result.get(5).getRank()), // 20 pts
                () -> assertEquals(6, result.get(6).getRank()), // 20 pts
                () -> assertEquals(6, result.get(7).getRank()), // 20 pts
                () -> assertEquals(9, result.get(8).getRank()) // 10 pts
                );

        verify(myStampService, times(1)).getUserAccumulatedStamps(storeId);
    }

    @Test
    @DisplayName("고객 누적 스탬프 순위 조회 - 빈 리스트")
    void getCustomerRankingByAccumulatedStamp_emptyList() {
        // given
        Long storeId = 1L;
        List<UserAccumulatedStampsDto> emptyList = Collections.emptyList();

        when(myStampService.getUserAccumulatedStamps(storeId)).thenReturn(emptyList);

        // when
        List<UserStampRankDto> result =
                customerAnalysisService.getCustomerRankingByAccumulatedStamp(storeId);

        // then
        assertTrue(result.isEmpty());
        verify(myStampService, times(1)).getUserAccumulatedStamps(storeId);
    }

    @Test
    @DisplayName("고객 누적 스탬프 순위 조회 - 단일 사용자")
    void getCustomerRankingByAccumulatedStamp_singleUser() {
        // given
        Long storeId = 1L;
        List<UserAccumulatedStampsDto> singleUser =
                Arrays.asList(new UserAccumulatedStampsDto(1L, 100));

        when(myStampService.getUserAccumulatedStamps(storeId)).thenReturn(singleUser);

        // when
        List<UserStampRankDto> result =
                customerAnalysisService.getCustomerRankingByAccumulatedStamp(storeId);

        // then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(100, result.get(0).getAccumulatedStampAmount());
        assertEquals(1, result.get(0).getRank());

        verify(myStampService, times(1)).getUserAccumulatedStamps(storeId);
    }
}
