package kr.kro.deom.domain.stampPolicy.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.stampPolicy.dto.StampPolicyRequest;
import kr.kro.deom.domain.stampPolicy.dto.StampPolicyResponse;
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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StampPolicyServiceTest {

    private StampPolicyService stampPolicyService;

    private StoreRepository storeRepository;
    private StampPolicyRepository stampPolicyRepository;

    @BeforeEach
    void setUp() {
        storeRepository = mock(StoreRepository.class);
        stampPolicyRepository = mock(StampPolicyRepository.class);
        stampPolicyService = new StampPolicyService(storeRepository, stampPolicyRepository);
    }

    @Test
    @DisplayName("스탬프 정책 생성 성공")
    void createPolicy_success() {
        // given
        Long userId = 1L;
        Long storeId = 10L;
        int baseAmount = 5000;
        int stampCount = 3;

        StampPolicyRequest request = new StampPolicyRequest(storeId, baseAmount, stampCount);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(storeId, userId))
                    .thenReturn(Optional.of(mock(Store.class)));

            when(stampPolicyRepository.existsByStoreIdAndBaseAmountAndDeletedAtIsNull(
                            storeId, baseAmount))
                    .thenReturn(false);

            when(stampPolicyRepository.save(any(StampPolicy.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0)); // 저장된 객체 그대로 반환

            // when
            StampPolicyResponse response = stampPolicyService.createStampPolicy(request);

            // then
            assertEquals(baseAmount, response.baseAmount());
            assertEquals(stampCount, response.stampCount());
        }
    }

    @Test
    @DisplayName("소유권 없는 가게에 정책 생성 시 예외 발생")
    void createPolicy_fail_noStorePermission() {
        // given
        Long userId = 1L;
        Long storeId = 999L;
        StampPolicyRequest request = new StampPolicyRequest(storeId, 5000, 3);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(storeId, userId))
                    .thenReturn(Optional.empty());

            // expect
            StoreException ex =
                    assertThrows(
                            StoreException.class,
                            () -> stampPolicyService.createStampPolicy(request));

            assertThat(ex.getBaseResponseCode()).isEqualTo(CommonErrorCode.NO_PERMISSION_FOR_STORE);
        }
    }

    @Test
    @DisplayName("이미 등록된 정책이 있을 경우 예외 발생")
    void createPolicy_fail_duplicate() {
        // given
        Long userId = 1L;
        Long storeId = 10L;
        int baseAmount = 10000;
        int stampCount = 2;

        StampPolicyRequest request = new StampPolicyRequest(storeId, baseAmount, stampCount);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(storeId, userId))
                    .thenReturn(Optional.of(mock(Store.class)));

            when(stampPolicyRepository.existsByStoreIdAndBaseAmountAndDeletedAtIsNull(
                            storeId, baseAmount))
                    .thenReturn(true);

            // expect
            StampPolicyException ex =
                    assertThrows(
                            StampPolicyException.class,
                            () -> stampPolicyService.createStampPolicy(request));

            assertThat(ex.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.ALREADY_REGISTERED_STAMP_POLICY);
        }
    }
}
