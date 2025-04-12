package kr.kro.deom.domain.stampPolicy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
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
    private final Long policyId = 1L;
    private final int baseAmount = 10000;
    private final int stampCount = 10;

    private Store store;
    private StampPolicy stampPolicy;
    private StampPolicyRequest createRequest;
    private StampPolicyUpdateRequest updateRequest;
    private List<StampPolicyDto> policyDtoList;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        store = new Store(); // 가정: Store 클래스가 기본 생성자를 가지고 있음

        stampPolicy = StampPolicy.create(storeId, baseAmount, stampCount);

        createRequest = new StampPolicyRequest(storeId, baseAmount, stampCount);
        updateRequest = new StampPolicyUpdateRequest(storeId, baseAmount + 5000, stampCount + 2);

        StampPolicyDto policyDto = new StampPolicyDto(policyId, baseAmount, stampCount);
        policyDtoList = List.of(policyDto);
    }

    @Test
    @DisplayName("스탬프 정책 목록 조회 성공")
    void getStampPolicy_Success() {
        // given
        when(stampPolicyRepository.findPoliciesByStoreId(storeId)).thenReturn(policyDtoList);

        // when
        List<StampPolicyDto> result = stampPolicyService.getStampPolicy(storeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).baseAmount()).isEqualTo(baseAmount);
        assertThat(result.get(0).stampCount()).isEqualTo(stampCount);

        verify(stampPolicyRepository).findPoliciesByStoreId(storeId);
    }

    @Test
    @DisplayName("스탬프 정책 생성 성공")
    void createStampPolicy_Success() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId)))
                    .thenReturn(Optional.of(store));
            when(stampPolicyRepository.existsByStoreIdAndBaseAmountAndDeletedAtIsNull(
                            eq(storeId), eq(baseAmount)))
                    .thenReturn(false);

            // 저장 후 반환되는 객체 설정
            StampPolicy savedPolicy = StampPolicy.create(storeId, baseAmount, stampCount);
            // 리플렉션이나 다른 방법으로 id 설정이 필요하다면 여기서 처리
            when(stampPolicyRepository.save(any(StampPolicy.class))).thenReturn(savedPolicy);

            // when
            StampPolicyResponse response = stampPolicyService.createStampPolicy(createRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.storeId()).isEqualTo(storeId);
            assertThat(response.baseAmount()).isEqualTo(baseAmount);
            assertThat(response.stampCount()).isEqualTo(stampCount);

            verify(storeRepository).findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId));
            verify(stampPolicyRepository)
                    .existsByStoreIdAndBaseAmountAndDeletedAtIsNull(eq(storeId), eq(baseAmount));
            verify(stampPolicyRepository).save(any(StampPolicy.class));
        }
    }

    @Test
    @DisplayName("스탬프 정책 생성 실패 - 가게 소유권 없음")
    void createStampPolicy_Fail_NoStoreOwnership() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId)))
                    .thenReturn(Optional.empty());

            // when & then
            StoreException exception =
                    assertThrows(
                            StoreException.class,
                            () -> stampPolicyService.createStampPolicy(createRequest));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.NO_PERMISSION_FOR_STORE);

            verify(storeRepository).findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId));
            verify(stampPolicyRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("스탬프 정책 생성 실패 - 이미 존재하는 정책")
    void createStampPolicy_Fail_DuplicatePolicy() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId)))
                    .thenReturn(Optional.of(store));
            when(stampPolicyRepository.existsByStoreIdAndBaseAmountAndDeletedAtIsNull(
                            eq(storeId), eq(baseAmount)))
                    .thenReturn(true);

            // when & then
            StampPolicyException exception =
                    assertThrows(
                            StampPolicyException.class,
                            () -> stampPolicyService.createStampPolicy(createRequest));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.ALREADY_REGISTERED_STAMP_POLICY);

            verify(storeRepository).findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId));
            verify(stampPolicyRepository)
                    .existsByStoreIdAndBaseAmountAndDeletedAtIsNull(eq(storeId), eq(baseAmount));
            verify(stampPolicyRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("스탬프 정책 수정 성공")
    void updateStampPolicy_Success() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId)))
                    .thenReturn(Optional.of(store));

            // 수정할 StampPolicy 객체 생성
            StampPolicy policyToUpdate = spy(StampPolicy.create(storeId, baseAmount, stampCount));
            when(stampPolicyRepository.findByIdAndDeletedAtIsNull(eq(policyId)))
                    .thenReturn(Optional.of(policyToUpdate));

            // when
            StampPolicyResponse response =
                    stampPolicyService.updateStampPolicy(policyId, updateRequest);

            // then
            assertThat(response).isNotNull();

            verify(storeRepository).findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId));
            verify(stampPolicyRepository).findByIdAndDeletedAtIsNull(eq(policyId));
            verify(policyToUpdate)
                    .update(eq(updateRequest.baseAmount()), eq(updateRequest.stampCount()));
        }
    }

    @Test
    @DisplayName("스탬프 정책 수정 실패 - 가게 소유권 없음")
    void updateStampPolicy_Fail_NoStoreOwnership() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId)))
                    .thenReturn(Optional.empty());

            // when & then
            StoreException exception =
                    assertThrows(
                            StoreException.class,
                            () -> stampPolicyService.updateStampPolicy(policyId, updateRequest));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.NO_PERMISSION_FOR_STORE);

            verify(storeRepository).findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId));
            verify(stampPolicyRepository, never()).findByIdAndDeletedAtIsNull(any());
        }
    }

    @Test
    @DisplayName("스탬프 정책 수정 실패 - 유효하지 않은 정책")
    void updateStampPolicy_Fail_InvalidPolicy() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId)))
                    .thenReturn(Optional.of(store));
            when(stampPolicyRepository.findByIdAndDeletedAtIsNull(eq(policyId)))
                    .thenReturn(Optional.empty());

            // when & then
            StampPolicyException exception =
                    assertThrows(
                            StampPolicyException.class,
                            () -> stampPolicyService.updateStampPolicy(policyId, updateRequest));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.INVALID_STAMP_POLICY);

            verify(storeRepository).findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId));
            verify(stampPolicyRepository).findByIdAndDeletedAtIsNull(eq(policyId));
        }
    }

    @Test
    @DisplayName("스탬프 정책 삭제 성공")
    void deleteStampPolicy_Success() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId)))
                    .thenReturn(Optional.of(store));

            // 삭제할 StampPolicy 객체 생성
            StampPolicy policyToDelete = spy(StampPolicy.create(storeId, baseAmount, stampCount));
            when(stampPolicyRepository.findByIdAndDeletedAtIsNull(eq(policyId)))
                    .thenReturn(Optional.of(policyToDelete));

            // when
            stampPolicyService.deleteStampPolicy(policyId, storeId);

            // then
            verify(storeRepository).findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId));
            verify(stampPolicyRepository).findByIdAndDeletedAtIsNull(eq(policyId));
            verify(policyToDelete).markAsDeleted();
        }
    }

    @Test
    @DisplayName("스탬프 정책 삭제 실패 - 가게 소유권 없음")
    void deleteStampPolicy_Fail_NoStoreOwnership() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId)))
                    .thenReturn(Optional.empty());

            // when & then
            StoreException exception =
                    assertThrows(
                            StoreException.class,
                            () -> stampPolicyService.deleteStampPolicy(policyId, storeId));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.NO_PERMISSION_FOR_STORE);

            verify(storeRepository).findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId));
            verify(stampPolicyRepository, never()).findByIdAndDeletedAtIsNull(any());
        }
    }

    @Test
    @DisplayName("스탬프 정책 삭제 실패 - 유효하지 않은 정책")
    void deleteStampPolicy_Fail_InvalidPolicy() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId)))
                    .thenReturn(Optional.of(store));
            when(stampPolicyRepository.findByIdAndDeletedAtIsNull(eq(policyId)))
                    .thenReturn(Optional.empty());

            // when & then
            StampPolicyException exception =
                    assertThrows(
                            StampPolicyException.class,
                            () -> stampPolicyService.deleteStampPolicy(policyId, storeId));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.INVALID_STAMP_POLICY);

            verify(storeRepository).findByIdAndOwnerIdAndIsDeletedFalse(eq(storeId), eq(userId));
            verify(stampPolicyRepository).findByIdAndDeletedAtIsNull(eq(policyId));
        }
    }
}
