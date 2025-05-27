package kr.kro.deom.domain.deom.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.*;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.deom.dto.*;
import kr.kro.deom.domain.deom.entity.Deom;
import kr.kro.deom.domain.deom.exception.DeomException;
import kr.kro.deom.domain.deom.repository.DeomRepository;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.repository.StoreRepository;
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
class DeomServiceTest {

    @InjectMocks private DeomService deomService;
    @Mock private DeomRepository deomRepository;
    @Mock private StoreRepository storeRepository;

    private final Long userId = 1L;
    private final Long storeId = 1L;
    private final Long deomId = 1L;
    private final String deomName = "테스트 디옴";
    private final int requiredStampAmount = 10;

    private Store store;

    @BeforeEach
    void setUp() {
        store = new Store();
    }

    @Nested
    @DisplayName("덤 정책 조회 테스트")
    class GetDeomPolicyTest {

        @Test
        @DisplayName("덤 정책 목록 조회 성공")
        void getDeomPolicy_Success() {
            // given
            List<DeomDto> expectedDtos =
                    Arrays.asList(new DeomDto(1L, "디옴1", 10), new DeomDto(2L, "디옴2", 20));

            when(deomRepository.findPoliciesByStoreIdAndDeletedAtIsNull(storeId))
                    .thenReturn(expectedDtos);

            // when
            List<DeomDto> result = deomService.getDeomPolicy(storeId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("디옴1");
            assertThat(result.get(0).requiredStampAmount()).isEqualTo(10);
            assertThat(result.get(1).name()).isEqualTo("디옴2");
            assertThat(result.get(1).requiredStampAmount()).isEqualTo(20);

            verify(deomRepository).findPoliciesByStoreIdAndDeletedAtIsNull(storeId);
        }

        @Test
        @DisplayName("빈 목록 조회 성공")
        void getDeomPolicy_EmptyList_Success() {
            // given
            when(deomRepository.findPoliciesByStoreIdAndDeletedAtIsNull(storeId))
                    .thenReturn(Collections.emptyList());

            // when
            List<DeomDto> result = deomService.getDeomPolicy(storeId);

            // then
            assertThat(result).isEmpty();
            verify(deomRepository).findPoliciesByStoreIdAndDeletedAtIsNull(storeId);
        }
    }

    @Nested
    @DisplayName("덤 정책 전체 업데이트 테스트")
    class UpdateAllDeomPoliciesTest {

        @Test
        @DisplayName("새 정책 생성 성공")
        void updateAllDeomPolicies_CreateNew_Success() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerId(storeId, userId))
                        .thenReturn(Optional.of(store));
                when(deomRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Collections.emptyList());
                when(deomRepository.existsByStoreIdAndNameAndDeletedAtIsNull(
                                eq(storeId), anyString()))
                        .thenReturn(false);

                Deom newPolicy1 = Deom.create(storeId, "디옴1", 10);
                Deom newPolicy2 = Deom.create(storeId, "디옴2", 20);

                when(deomRepository.saveAll(any()))
                        .thenReturn(Arrays.asList(newPolicy1, newPolicy2));

                List<DeomDto> requestPolicies =
                        Arrays.asList(new DeomDto(null, "디옴1", 10), new DeomDto(null, "디옴2", 20));
                DeomsRequest request = new DeomsRequest(requestPolicies);

                // when
                DeomsResponse response = deomService.updateAllDeomPolicies(storeId, request);

                // then
                assertThat(response.policies()).hasSize(2);
                assertThat(response.policies().get(0).name()).isEqualTo("디옴1");
                assertThat(response.policies().get(1).name()).isEqualTo("디옴2");

                verify(deomRepository).saveAll(any());
                verify(storeRepository).findByIdAndOwnerId(storeId, userId);
            }
        }

        @Test
        @DisplayName("기존 정책 업데이트 성공")
        void updateAllDeomPolicies_UpdateExisting_Success() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerId(storeId, userId))
                        .thenReturn(Optional.of(store));

                Deom existingPolicy = spy(Deom.create(storeId, "기존디옴", 10));
                when(existingPolicy.getId()).thenReturn(1L);

                when(deomRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Arrays.asList(existingPolicy));

                List<DeomDto> requestPolicies =
                        Arrays.asList(
                                new DeomDto(1L, "수정된디옴", 15) // 기존 정책 업데이트
                                );
                DeomsRequest request = new DeomsRequest(requestPolicies);

                // when
                DeomsResponse response = deomService.updateAllDeomPolicies(storeId, request);

                // then
                assertThat(response.policies()).hasSize(1);
                verify(existingPolicy).update("수정된디옴", 15);
                verify(deomRepository, never()).saveAll(any()); // 새 정책 없으므로 saveAll 호출 안됨
            }
        }

        @Test
        @DisplayName("기존 정책 삭제 마킹 성공")
        void updateAllDeomPolicies_DeleteExisting_Success() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerId(storeId, userId))
                        .thenReturn(Optional.of(store));

                Deom existingPolicy1 = spy(Deom.create(storeId, "디옴1", 10));
                Deom existingPolicy2 = spy(Deom.create(storeId, "디옴2", 20));
                when(existingPolicy1.getId()).thenReturn(1L);
                when(existingPolicy2.getId()).thenReturn(2L);

                when(deomRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Arrays.asList(existingPolicy1, existingPolicy2));

                // 첫 번째 정책만 유지, 두 번째는 삭제됨
                List<DeomDto> requestPolicies = Arrays.asList(new DeomDto(1L, "디옴1", 10));
                DeomsRequest request = new DeomsRequest(requestPolicies);

                // when
                DeomsResponse response = deomService.updateAllDeomPolicies(storeId, request);

                // then
                assertThat(response.policies()).hasSize(1);
                assertThat(response.policies().get(0).id()).isEqualTo(1L);

                verify(existingPolicy2).markAsDeleted(); // 두 번째 정책이 삭제 마킹됨
                verify(existingPolicy1, never()).markAsDeleted(); // 첫 번째는 삭제 안됨
            }
        }

        @Test
        @DisplayName("복합 시나리오: 생성, 업데이트, 삭제 동시 수행")
        void updateAllDeomPolicies_ComplexScenario_Success() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerId(storeId, userId))
                        .thenReturn(Optional.of(store));

                // 기존 정책 2개
                Deom existingPolicy1 = spy(Deom.create(storeId, "기존디옴1", 10));
                Deom existingPolicy2 = spy(Deom.create(storeId, "기존디옴2", 20));
                when(existingPolicy1.getId()).thenReturn(1L);
                when(existingPolicy2.getId()).thenReturn(2L);

                when(deomRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Arrays.asList(existingPolicy1, existingPolicy2));
                when(deomRepository.existsByStoreIdAndNameAndDeletedAtIsNull(storeId, "새디옴"))
                        .thenReturn(false);

                // 새 정책 생성
                Deom newPolicy = Deom.create(storeId, "새디옴", 30);
                when(deomRepository.saveAll(any())).thenReturn(Arrays.asList(newPolicy));

                List<DeomDto> requestPolicies =
                        Arrays.asList(
                                new DeomDto(1L, "수정된디옴1", 15), // 기존 정책1 업데이트
                                new DeomDto(null, "새디옴", 30) // 새 정책 생성
                                // 기존 정책2는 요청에 없으므로 삭제됨
                                );
                DeomsRequest request = new DeomsRequest(requestPolicies);

                // when
                DeomsResponse response = deomService.updateAllDeomPolicies(storeId, request);

                // then
                assertThat(response.policies()).hasSize(2);

                verify(existingPolicy1).update("수정된디옴1", 15); // 업데이트됨
                verify(existingPolicy2).markAsDeleted(); // 삭제 마킹됨
                verify(deomRepository).saveAll(any()); // 새 정책 저장됨
            }
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("요청 내 중복된 이름으로 인한 실패")
        void updateAllDeomPolicies_DuplicateNameInRequest_ThrowsException() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerId(storeId, userId))
                        .thenReturn(Optional.of(store));

                List<DeomDto> requestPolicies =
                        Arrays.asList(
                                new DeomDto(null, "중복이름", 10),
                                new DeomDto(null, "중복이름", 20) // 같은 이름
                                );
                DeomsRequest request = new DeomsRequest(requestPolicies);

                // when & then
                DeomException exception =
                        assertThrows(
                                DeomException.class,
                                () -> deomService.updateAllDeomPolicies(storeId, request));

                assertThat(exception.getBaseResponseCode())
                        .isEqualTo(CommonErrorCode.ALREADY_REGISTERED_DEOM);
            }
        }

        @Test
        @DisplayName("기존 정책과 중복된 이름으로 신규 생성 시도 실패")
        void updateAllDeomPolicies_DuplicateNameWithExisting_ThrowsException() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerId(storeId, userId))
                        .thenReturn(Optional.of(store));

                Deom existingPolicy = Deom.create(storeId, "기존디옴", 10);
                when(deomRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Arrays.asList(existingPolicy));
                when(deomRepository.existsByStoreIdAndNameAndDeletedAtIsNull(storeId, "기존덤"))
                        .thenReturn(true);

                List<DeomDto> requestPolicies =
                        Arrays.asList(
                                new DeomDto(null, "기존덤", 20) // 기존과 같은 이름으로 새 정책 생성 시도
                                );
                DeomsRequest request = new DeomsRequest(requestPolicies);

                // when & then

                DeomException exception =
                        assertThrows(
                                DeomException.class,
                                () -> deomService.updateAllDeomPolicies(storeId, request));

                assertThat(exception.getBaseResponseCode())
                        .isEqualTo(CommonErrorCode.ALREADY_REGISTERED_DEOM);
            }
        }

        @Test
        @DisplayName("존재하지 않는 스토어로 인한 권한 실패")
        void updateAllDeomPolicies_NoStorePermission_ThrowsException() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerId(storeId, userId))
                        .thenReturn(Optional.empty());

                List<DeomDto> requestPolicies = Arrays.asList(new DeomDto(null, "디옴", 10));
                DeomsRequest request = new DeomsRequest(requestPolicies);

                // when & then
                DeomException exception =
                        assertThrows(
                                DeomException.class,
                                () -> deomService.updateAllDeomPolicies(storeId, request));

                assertThat(exception.getBaseResponseCode())
                        .isEqualTo(CommonErrorCode.NO_PERMISSION_FOR_STORE);
            }
        }

        @Test
        @DisplayName("존재하지 않는 정책 ID로 인한 실패")
        void updateAllDeomPolicies_InvalidPolicyId_ThrowsException() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerId(storeId, userId))
                        .thenReturn(Optional.of(store));
                when(deomRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Collections.emptyList());

                List<DeomDto> requestPolicies =
                        Arrays.asList(
                                new DeomDto(999L, "디옴", 10) // 존재하지 않는 ID
                                );
                DeomsRequest request = new DeomsRequest(requestPolicies);

                // when & then

                DeomException exception =
                        assertThrows(
                                DeomException.class,
                                () -> deomService.updateAllDeomPolicies(storeId, request));

                assertThat(exception.getBaseResponseCode())
                        .isEqualTo(CommonErrorCode.INVALID_DEOM_POLICY);
            }
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTest {

        @Test
        @DisplayName("빈 요청으로 모든 기존 정책 삭제")
        void updateAllDeomPolicies_EmptyRequest_DeletesAllExisting() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerId(storeId, userId))
                        .thenReturn(Optional.of(store));

                Deom existingPolicy = spy(Deom.create(storeId, "기존디옴", 10));
                when(existingPolicy.getId()).thenReturn(1L);

                when(deomRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Arrays.asList(existingPolicy));

                DeomsRequest request = new DeomsRequest(Collections.emptyList());

                // when
                DeomsResponse response = deomService.updateAllDeomPolicies(storeId, request);

                // then
                assertThat(response.policies()).isEmpty();
                verify(existingPolicy).markAsDeleted();
            }
        }

        @Test
        @DisplayName("동일한 값으로 업데이트 시도")
        void updateAllDeomPolicies_SameValues_Success() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerId(storeId, userId))
                        .thenReturn(Optional.of(store));

                Deom existingPolicy = spy(Deom.create(storeId, deomName, requiredStampAmount));
                when(existingPolicy.getId()).thenReturn(1L);
                when(existingPolicy.getName()).thenReturn(deomName);
                when(existingPolicy.getRequiredStampAmount()).thenReturn(requiredStampAmount);

                when(deomRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Arrays.asList(existingPolicy));

                List<DeomDto> requestPolicies =
                        Arrays.asList(
                                new DeomDto(1L, deomName, requiredStampAmount) // 기존과 동일한 값
                                );
                DeomsRequest request = new DeomsRequest(requestPolicies);

                // when
                DeomsResponse response = deomService.updateAllDeomPolicies(storeId, request);

                // then
                assertThat(response.policies()).hasSize(1);
                assertThat(response.policies().get(0).name()).isEqualTo(deomName);
                assertThat(response.policies().get(0).requiredStampAmount())
                        .isEqualTo(requiredStampAmount);

                verify(existingPolicy)
                        .update(deomName, requiredStampAmount); // 호출은 되지만 JPA가 더티 체킹으로 최적화
            }
        }

        @Test
        @DisplayName("대량 정책 처리 시나리오")
        void updateAllDeomPolicies_LargeDataSet_Success() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerId(storeId, userId))
                        .thenReturn(Optional.of(store));
                when(deomRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Collections.emptyList());
                when(deomRepository.existsByStoreIdAndNameAndDeletedAtIsNull(
                                eq(storeId), anyString()))
                        .thenReturn(false);

                List<DeomDto> requestPolicies = new ArrayList<>();
                List<Deom> savedPolicies = new ArrayList<>();

                for (int i = 1; i <= 100; i++) {
                    requestPolicies.add(new DeomDto(null, "디옴" + i, i));
                    savedPolicies.add(Deom.create(storeId, "디옴" + i, i));
                }

                when(deomRepository.saveAll(any())).thenReturn(savedPolicies);
                DeomsRequest request = new DeomsRequest(requestPolicies);

                // when
                DeomsResponse response = deomService.updateAllDeomPolicies(storeId, request);

                // then
                assertThat(response.policies()).hasSize(100);
                verify(deomRepository).saveAll(any(List.class));
            }
        }
    }
}
