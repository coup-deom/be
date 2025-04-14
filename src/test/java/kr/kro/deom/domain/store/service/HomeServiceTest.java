package kr.kro.deom.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.deom.entity.Deom;
import kr.kro.deom.domain.deom.repository.DeomRepository;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.store.dto.response.DeomStatus;
import kr.kro.deom.domain.store.dto.response.StoreResponse;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.repository.StoreRepository;
import kr.kro.deom.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HomeServiceTest {

    @Mock private StoreRepository storeRepository;

    @Mock private DeomRepository deomRepository;

    @Mock private MyStampRepository myStampRepository;

    @Mock private UserRepository userRepository;

    @InjectMocks private HomeService homeService;

    private final Long TEST_USER_ID = 1L;
    private List<Store> stores;
    private List<Deom> deoms;

    @BeforeEach
    void setUp() {
        // 테스트용 스토어 데이터 설정
        stores =
                Arrays.asList(
                        createStore(1L, "카페1", "강남점"),
                        createStore(2L, "카페2", "홍대점"),
                        createStore(3L, "레스토랑", "종로점"));

        // 테스트용 덤 데이터 설정
        deoms =
                Arrays.asList(
                        createDeom(1L, 1L, "아메리카노", 10),
                        createDeom(2L, 1L, "라떼", 15),
                        createDeom(3L, 2L, "스무디", 8),
                        createDeom(4L, 3L, "파스타", 12));
    }

    private Store createStore(Long id, String name, String branch) {
        Store store = new Store();
        store.setId(id);
        store.setStoreName(name);
        store.setBranchName(branch);
        store.setImage("image_" + id + ".jpg");
        store.setAddressCity("서울");
        store.setAddressStreet("테스트 거리 " + id);
        store.setAddressDetail("상세주소 " + id);
        return store;
    }

    private Deom createDeom(Long id, Long storeId, String name, Integer requiredStampAmount) {
        Deom deom = new Deom();
        deom.setId(id);
        deom.setStoreId(storeId);
        deom.setName(name);
        deom.setRequiredStampAmount(requiredStampAmount);
        return deom;
    }

    @Nested
    @DisplayName("getAllStores 메서드 테스트")
    class GetAllStoresTest {

        @Test
        @DisplayName("모든 스토어를 올바르게 조회한다")
        void getAllStores_ReturnsAllStores() {
            // given
            when(storeRepository.findAll()).thenReturn(stores);

            // findByStoreIdOrderByRequiredStampAmountAsc 호출을 위한 설정
            when(deomRepository.findByStoreIdOrderByRequiredStampAmountAsc(anyLong()))
                    .thenAnswer(
                            invocation -> {
                                Long storeId = invocation.getArgument(0);
                                return deoms.stream()
                                        .filter(deom -> deom.getStoreId().equals(storeId))
                                        .toList();
                            });

            // 스탬프 개수 모의 설정
            when(myStampRepository.findStampAmountByUserIdAndStoreId(eq(TEST_USER_ID), anyLong()))
                    .thenReturn(10); // 모든 스토어의 스탬프 개수는 5로 가정

            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                // when
                List<StoreResponse> result = homeService.getAllStores();

                // then
                assertThat(result).hasSize(3);
                assertThat(result.get(0).getStoreId()).isEqualTo(1L);
                assertThat(result.get(1).getStoreId()).isEqualTo(2L);
                assertThat(result.get(2).getStoreId()).isEqualTo(3L);

                // 모든 스토어가 스탬프 5개를 가지고 있는지 확인
                assertThat(result).allMatch(store -> store.getMyStampCount() == 10);

                // 첫 번째 스토어(id=1)의 디옴 상태 확인
                List<StoreResponse.DeomInfo> store1Deoms = result.get(0).getDeoms();
                assertThat(store1Deoms).hasSize(2);
                // 아메리카노(필요 스탬프 10) > 스탬프 5개이므로 IN_PROGRESS
                assertThat(store1Deoms.get(0).getStatus()).isEqualTo(DeomStatus.AVAILABLE);

                assertThat(store1Deoms.get(1).getStatus()).isEqualTo(DeomStatus.IN_PROGRESS);

                // 두 번째 스토어(id=2)의 디옴 상태 확인
                List<StoreResponse.DeomInfo> store2Deoms = result.get(1).getDeoms();
                assertThat(store2Deoms).hasSize(1);
                // 스무디(필요 스탬프 8) > 스탬프 5개이므로 IN_PROGRESS
                assertThat(store2Deoms.get(0).getStatus()).isEqualTo(DeomStatus.AVAILABLE);

                // 세 번째 스토어(id=3)의 디옴 상태 확인
                List<StoreResponse.DeomInfo> store3Deoms = result.get(2).getDeoms();
                assertThat(store3Deoms).hasSize(1);
                // 파스타(필요 스탬프 12) > 스탬프 5개이므로 IN_PROGRESS
                assertThat(store3Deoms.get(0).getStatus()).isEqualTo(DeomStatus.IN_PROGRESS);
            }
        }

        @Test
        @DisplayName("스토어가 없는 경우 빈 리스트를 반환한다")
        void getAllStores_WhenNoStores_ReturnsEmptyList() {
            // given
            when(storeRepository.findAll()).thenReturn(Collections.emptyList());

            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                // when
                List<StoreResponse> result = homeService.getAllStores();

                // then
                assertThat(result).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("getStampedStores 메서드 테스트")
    class GetStampedStoresTest {

        @Test
        @DisplayName("스탬프가 있는 스토어만 조회한다")
        void getStampedStores_ReturnsOnlyStoresWithStamps() {
            // given
            when(storeRepository.findAll()).thenReturn(stores);

            when(deomRepository.findByStoreIdOrderByRequiredStampAmountAsc(anyLong()))
                    .thenAnswer(
                            invocation -> {
                                Long storeId = invocation.getArgument(0);
                                return deoms.stream()
                                        .filter(deom -> deom.getStoreId().equals(storeId))
                                        .toList();
                            });

            // 스토어별로 다른 스탬프 개수 설정 (0은 스탬프 없음)
            when(myStampRepository.findStampAmountByUserIdAndStoreId(eq(TEST_USER_ID), eq(1L)))
                    .thenReturn(5);
            when(myStampRepository.findStampAmountByUserIdAndStoreId(eq(TEST_USER_ID), eq(2L)))
                    .thenReturn(0);
            when(myStampRepository.findStampAmountByUserIdAndStoreId(eq(TEST_USER_ID), eq(3L)))
                    .thenReturn(3);

            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                // when
                List<StoreResponse> result = homeService.getStampedStores();

                // then
                assertThat(result).hasSize(2); // 스탬프가 있는 스토어 2개만 반환

                // 스토어 ID 확인
                assertThat(result).extracting("storeId").containsExactly(1L, 3L);

                // 스탬프 개수 확인
                assertThat(result.get(0).getMyStampCount()).isEqualTo(5);
                assertThat(result.get(1).getMyStampCount()).isEqualTo(3);
            }
        }

        @Test
        @DisplayName("스탬프가 있는 스토어가 없으면 빈 리스트를 반환한다")
        void getStampedStores_WhenNoStamped_ReturnsEmptyList() {
            // given
            when(storeRepository.findAll()).thenReturn(stores);

            // 모든 스토어의 스탬프 개수를 0으로 설정
            when(myStampRepository.findStampAmountByUserIdAndStoreId(eq(TEST_USER_ID), anyLong()))
                    .thenReturn(0);

            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                // when
                List<StoreResponse> result = homeService.getStampedStores();

                // then
                assertThat(result).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("getAvailableStores 메서드 테스트")
    class GetAvailableStoresTest {

        @Test
        @DisplayName("사용 가능한 덤이 있는 스토어만 조회한다")
        void getAvailableStores_ReturnsOnlyStoresWithAvailableDeoms() {
            // given
            when(storeRepository.findAll()).thenReturn(stores);

            when(deomRepository.findByStoreIdOrderByRequiredStampAmountAsc(anyLong()))
                    .thenAnswer(
                            invocation -> {
                                Long storeId = invocation.getArgument(0);
                                return deoms.stream()
                                        .filter(deom -> deom.getStoreId().equals(storeId))
                                        .toList();
                            });

            // 스토어별로 다른 스탬프 개수 설정
            when(myStampRepository.findStampAmountByUserIdAndStoreId(eq(TEST_USER_ID), eq(1L)))
                    .thenReturn(15); // 라떼(15)까지 가능
            when(myStampRepository.findStampAmountByUserIdAndStoreId(eq(TEST_USER_ID), eq(2L)))
                    .thenReturn(5); // 스무디(8) 불가능
            when(myStampRepository.findStampAmountByUserIdAndStoreId(eq(TEST_USER_ID), eq(3L)))
                    .thenReturn(12); // 파스타(12) 가능

            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                // when
                List<StoreResponse> result = homeService.getAvailableStores();

                // then
                assertThat(result).hasSize(2); // 사용 가능한 덤이 있는 스토어 2개만 반환

                // 스토어 ID 확인
                assertThat(result).extracting("storeId").containsExactly(1L, 3L);

                // 첫 번째 스토어의 덤 상태 확인
                List<StoreResponse.DeomInfo> deomInfos = result.get(0).getDeoms();
                assertThat(deomInfos).hasSize(2);
                assertThat(deomInfos.get(0).getStatus())
                        .isEqualTo(DeomStatus.AVAILABLE); // 아메리카노(10) 가능
                assertThat(deomInfos.get(1).getStatus())
                        .isEqualTo(DeomStatus.AVAILABLE); // 라떼(15) 가능

                // 두 번째 스토어의 덤 상태 확인
                deomInfos = result.get(1).getDeoms();
                assertThat(deomInfos).hasSize(1);
                assertThat(deomInfos.get(0).getStatus())
                        .isEqualTo(DeomStatus.AVAILABLE); // 파스타(12) 가능
            }
        }

        @Test
        @DisplayName("사용 가능한 덤이 있는 스토어가 없으면 빈 리스트를 반환한다")
        void getAvailableStores_WhenNoAvailable_ReturnsEmptyList() {
            // given
            when(storeRepository.findAll()).thenReturn(stores);

            when(deomRepository.findByStoreIdOrderByRequiredStampAmountAsc(anyLong()))
                    .thenAnswer(
                            invocation -> {
                                Long storeId = invocation.getArgument(0);
                                return deoms.stream()
                                        .filter(deom -> deom.getStoreId().equals(storeId))
                                        .toList();
                            });

            // 모든 스토어의 스탬프 개수를 디옴 요구량보다 적게 설정
            when(myStampRepository.findStampAmountByUserIdAndStoreId(eq(TEST_USER_ID), eq(1L)))
                    .thenReturn(5); // 아메리카노(10) 불가능
            when(myStampRepository.findStampAmountByUserIdAndStoreId(eq(TEST_USER_ID), eq(2L)))
                    .thenReturn(3); // 스무디(8) 불가능
            when(myStampRepository.findStampAmountByUserIdAndStoreId(eq(TEST_USER_ID), eq(3L)))
                    .thenReturn(7); // 파스타(12) 불가능

            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                // when
                List<StoreResponse> result = homeService.getAvailableStores();

                // then
                assertThat(result).isEmpty();
            }
        }
    }
}
