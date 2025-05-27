package kr.kro.deom.domain.stampPolicy.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.*;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.stampPolicy.dto.*;
import kr.kro.deom.domain.stampPolicy.entity.StampPolicy;
import kr.kro.deom.domain.stampPolicy.exception.StampPolicyException;
import kr.kro.deom.domain.stampPolicy.repository.StampPolicyRepository;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.exception.StoreException;
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
class StampPolicyServiceTest {

    @InjectMocks private StampPolicyService stampPolicyService;
    @Mock private StoreRepository storeRepository;
    @Mock private StampPolicyRepository stampPolicyRepository;

    private final Long userId = 1L;
    private final Long storeId = 1L;
    private Store store;

    @BeforeEach
    void setUp() {
        store = new Store();
    }

    @Nested
    @DisplayName("스탬프 정책 조회 테스트")
    class GetStampPolicyTest {

        @Test
        @DisplayName("스탬프 정책 목록 조회 성공")
        void getStampPolicy_Success() {
            // given
            List<StampPolicyDto> expectedDtos =
                    Arrays.asList(
                            new StampPolicyDto(1L, 10000, 10), new StampPolicyDto(2L, 20000, 20));

            when(stampPolicyRepository.findPoliciesByStoreId(storeId)).thenReturn(expectedDtos);

            // when
            List<StampPolicyDto> result = stampPolicyService.getStampPolicy(storeId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).baseAmount()).isEqualTo(10000);
            assertThat(result.get(0).stampCount()).isEqualTo(10);
            assertThat(result.get(1).baseAmount()).isEqualTo(20000);
            assertThat(result.get(1).stampCount()).isEqualTo(20);

            verify(stampPolicyRepository).findPoliciesByStoreId(storeId);
        }

        @Test
        @DisplayName("빈 목록 조회 성공")
        void getStampPolicy_EmptyList_Success() {
            // given
            when(stampPolicyRepository.findPoliciesByStoreId(storeId))
                    .thenReturn(Collections.emptyList());

            // when
            List<StampPolicyDto> result = stampPolicyService.getStampPolicy(storeId);

            // then
            assertThat(result).isEmpty();
            verify(stampPolicyRepository).findPoliciesByStoreId(storeId);
        }
    }

    @Nested
    @DisplayName("스탬프 정책 전체 업데이트 테스트")
    class UpdateAllStampPoliciesTest {

        @Test
        @DisplayName("새 정책 생성 성공")
        void updateAllStampPolicies_CreateNew_Success() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(storeId, userId))
                        .thenReturn(Optional.of(store));
                when(stampPolicyRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Collections.emptyList());

                StampPolicy newPolicy1 = StampPolicy.create(storeId, 10000, 10);
                StampPolicy newPolicy2 = StampPolicy.create(storeId, 20000, 20);

                when(stampPolicyRepository.saveAll(any()))
                        .thenReturn(Arrays.asList(newPolicy1, newPolicy2));

                List<StampPolicyDto> requestPolicies =
                        Arrays.asList(
                                new StampPolicyDto(null, 10000, 10),
                                new StampPolicyDto(null, 20000, 20));
                StampPoliciesRequest request = new StampPoliciesRequest(requestPolicies);

                // when
                StampPoliciesResponse response =
                        stampPolicyService.updateAllStampPolicies(storeId, request);

                // then
                assertThat(response.policies()).hasSize(2);
                assertThat(response.policies().get(0).baseAmount()).isEqualTo(10000);
                assertThat(response.policies().get(1).baseAmount()).isEqualTo(20000);

                verify(stampPolicyRepository).saveAll(any());
                verify(storeRepository).findByIdAndOwnerIdAndIsDeletedFalse(storeId, userId);
            }
        }

        @Test
        @DisplayName("기존 정책 업데이트 성공")
        void updateAllStampPolicies_UpdateExisting_Success() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(storeId, userId))
                        .thenReturn(Optional.of(store));

                StampPolicy existingPolicy = spy(StampPolicy.create(storeId, 10000, 10));
                when(existingPolicy.getId()).thenReturn(1L);

                when(stampPolicyRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Arrays.asList(existingPolicy));

                List<StampPolicyDto> requestPolicies =
                        Arrays.asList(
                                new StampPolicyDto(1L, 15000, 15) // 기존 정책 업데이트
                                );
                StampPoliciesRequest request = new StampPoliciesRequest(requestPolicies);

                // when
                StampPoliciesResponse response =
                        stampPolicyService.updateAllStampPolicies(storeId, request);

                // then
                assertThat(response.policies()).hasSize(1);
                verify(existingPolicy).update(15000, 15);
                verify(stampPolicyRepository, never()).saveAll(any()); // 새 정책 없으므로 saveAll 호출 안됨
            }
        }

        @Test
        @DisplayName("기존 정책 삭제 마킹 성공")
        void updateAllStampPolicies_DeleteExisting_Success() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(storeId, userId))
                        .thenReturn(Optional.of(store));

                StampPolicy existingPolicy1 = spy(StampPolicy.create(storeId, 10000, 10));
                StampPolicy existingPolicy2 = spy(StampPolicy.create(storeId, 20000, 20));
                when(existingPolicy1.getId()).thenReturn(1L);
                when(existingPolicy2.getId()).thenReturn(2L);

                when(stampPolicyRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Arrays.asList(existingPolicy1, existingPolicy2));

                // 첫 번째 정책만 유지, 두 번째는 삭제됨
                List<StampPolicyDto> requestPolicies =
                        Arrays.asList(new StampPolicyDto(1L, 10000, 10));
                StampPoliciesRequest request = new StampPoliciesRequest(requestPolicies);

                // when
                StampPoliciesResponse response =
                        stampPolicyService.updateAllStampPolicies(storeId, request);

                // then
                assertThat(response.policies()).hasSize(1);
                assertThat(response.policies().get(0).id()).isEqualTo(1L);

                verify(existingPolicy2).markAsDeleted(); // 두 번째 정책이 삭제 마킹됨
                verify(existingPolicy1, never()).markAsDeleted(); // 첫 번째는 삭제 안됨
            }
        }

        @Test
        @DisplayName("복합 시나리오: 생성, 업데이트, 삭제 동시 수행")
        void updateAllStampPolicies_ComplexScenario_Success() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(storeId, userId))
                        .thenReturn(Optional.of(store));

                // 기존 정책 2개
                StampPolicy existingPolicy1 = spy(StampPolicy.create(storeId, 10000, 10));
                StampPolicy existingPolicy2 = spy(StampPolicy.create(storeId, 20000, 20));
                when(existingPolicy1.getId()).thenReturn(1L);
                when(existingPolicy2.getId()).thenReturn(2L);

                when(stampPolicyRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Arrays.asList(existingPolicy1, existingPolicy2));

                StampPolicy newPolicy = StampPolicy.create(storeId, 30000, 30);
                when(stampPolicyRepository.saveAll(any())).thenReturn(Arrays.asList(newPolicy));

                List<StampPolicyDto> requestPolicies =
                        Arrays.asList(
                                new StampPolicyDto(1L, 15000, 15), // 기존 정책1 업데이트
                                new StampPolicyDto(null, 30000, 30) // 새 정책 생성
                                // 기존 정책2는 요청에 없으므로 삭제됨
                                );
                StampPoliciesRequest request = new StampPoliciesRequest(requestPolicies);

                // when
                StampPoliciesResponse response =
                        stampPolicyService.updateAllStampPolicies(storeId, request);

                // then
                assertThat(response.policies()).hasSize(2);

                verify(existingPolicy1).update(15000, 15); // 업데이트됨
                verify(existingPolicy2).markAsDeleted(); // 삭제 마킹됨
                verify(stampPolicyRepository).saveAll(any()); // 새 정책 저장됨
            }
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("중복된 baseAmount로 인한 실패")
        void updateAllStampPolicies_DuplicateBaseAmount_ThrowsException() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(storeId, userId))
                        .thenReturn(Optional.of(store));

                List<StampPolicyDto> requestPolicies =
                        Arrays.asList(
                                new StampPolicyDto(null, 10000, 10),
                                new StampPolicyDto(null, 10000, 20) // 같은 baseAmount
                                );
                StampPoliciesRequest request = new StampPoliciesRequest(requestPolicies);

                // when & then

                StampPolicyException exception =
                        assertThrows(
                                StampPolicyException.class,
                                () -> stampPolicyService.updateAllStampPolicies(storeId, request));

                assertThat(exception.getBaseResponseCode())
                        .isEqualTo(CommonErrorCode.ALREADY_REGISTERED_STAMP_POLICY);
            }
        }

        @Test
        @DisplayName("존재하지 않는 스토어로 인한 권한 실패")
        void updateAllStampPolicies_NoStorePermission_ThrowsException() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(storeId, userId))
                        .thenReturn(Optional.empty());

                List<StampPolicyDto> requestPolicies =
                        Arrays.asList(new StampPolicyDto(null, 10000, 10));
                StampPoliciesRequest request = new StampPoliciesRequest(requestPolicies);

                StoreException exception =
                        assertThrows(
                                StoreException.class,
                                () -> stampPolicyService.updateAllStampPolicies(storeId, request));

                assertThat(exception.getBaseResponseCode())
                        .isEqualTo(CommonErrorCode.NO_PERMISSION_FOR_STORE);
            }
        }

        @Test
        @DisplayName("존재하지 않는 정책 ID로 인한 실패")
        void updateAllStampPolicies_InvalidPolicyId_ThrowsException() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(storeId, userId))
                        .thenReturn(Optional.of(store));
                when(stampPolicyRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Collections.emptyList());

                List<StampPolicyDto> requestPolicies =
                        Arrays.asList(
                                new StampPolicyDto(999L, 10000, 10) // 존재하지 않는 ID
                                );
                StampPoliciesRequest request = new StampPoliciesRequest(requestPolicies);

                // when & then
                StampPolicyException exception =
                        assertThrows(
                                StampPolicyException.class,
                                () -> stampPolicyService.updateAllStampPolicies(storeId, request));

                assertThat(exception.getBaseResponseCode())
                        .isEqualTo(CommonErrorCode.INVALID_STAMP_POLICY);
            }
        }
    }

    @Nested
    @DisplayName(" 빈 요청 케이스 테스트")
    class EdgeCaseTest {

        @Test
        @DisplayName("빈 요청으로 모든 기존 정책 삭제")
        void updateAllStampPolicies_EmptyRequest_DeletesAllExisting() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(storeId, userId))
                        .thenReturn(Optional.of(store));

                StampPolicy existingPolicy = spy(StampPolicy.create(storeId, 10000, 10));
                when(existingPolicy.getId()).thenReturn(1L);

                when(stampPolicyRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Arrays.asList(existingPolicy));

                StampPoliciesRequest request = new StampPoliciesRequest(Collections.emptyList());

                // when
                StampPoliciesResponse response =
                        stampPolicyService.updateAllStampPolicies(storeId, request);

                // then
                assertThat(response.policies()).isEmpty();
                verify(existingPolicy).markAsDeleted();
            }
        }

        @Test
        @DisplayName("동일한 값으로 업데이트 시도")
        void updateAllStampPolicies_SameValues_Success() {
            // given
            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

                when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(storeId, userId))
                        .thenReturn(Optional.of(store));

                StampPolicy existingPolicy = spy(StampPolicy.create(storeId, 10000, 10));
                when(existingPolicy.getId()).thenReturn(1L);
                when(existingPolicy.getBaseAmount()).thenReturn(10000);
                when(existingPolicy.getStampCount()).thenReturn(10);

                when(stampPolicyRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                        .thenReturn(Arrays.asList(existingPolicy));

                List<StampPolicyDto> requestPolicies =
                        Arrays.asList(
                                new StampPolicyDto(1L, 10000, 10) // 기존과 동일한 값
                                );
                StampPoliciesRequest request = new StampPoliciesRequest(requestPolicies);

                // when
                StampPoliciesResponse response =
                        stampPolicyService.updateAllStampPolicies(storeId, request);

                // then
                assertThat(response.policies()).hasSize(1);
                assertThat(response.policies().get(0).baseAmount()).isEqualTo(10000);
                assertThat(response.policies().get(0).stampCount()).isEqualTo(10);

                verify(existingPolicy).update(10000, 10); // 호출은 되지만 JPA가 더티 체킹으로 최적화
            }
        }
    }
}
