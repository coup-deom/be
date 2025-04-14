package kr.kro.deom.domain.deom.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.deom.dto.DeomDto;
import kr.kro.deom.domain.deom.dto.DeomRequest;
import kr.kro.deom.domain.deom.dto.DeomResponse;
import kr.kro.deom.domain.deom.dto.DeomUpdateRequest;
import kr.kro.deom.domain.deom.entity.Deom;
import kr.kro.deom.domain.deom.exception.DeomException;
import kr.kro.deom.domain.deom.repository.DeomRepository;
import kr.kro.deom.domain.store.entity.Store;
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
    private Deom deom;
    private DeomRequest createRequest;
    private DeomUpdateRequest updateRequest;
    private List<DeomDto> deomDtoList;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        store = new Store(); // 가정: Store 클래스가 기본 생성자를 가지고 있음

        deom = Deom.create(storeId, deomName, requiredStampAmount);

        createRequest = new DeomRequest(storeId, deomName, requiredStampAmount);
        updateRequest = new DeomUpdateRequest(storeId, "수정된 디옴", requiredStampAmount + 5);

        DeomDto deomDto = new DeomDto(deomId, deomName, requiredStampAmount);
        deomDtoList = List.of(deomDto);
    }

    @Test
    @DisplayName("디옴 정책 목록 조회 성공")
    void getDeomPolicy_Success() {
        // given
        when(deomRepository.findPoliciesByStoreId(storeId)).thenReturn(deomDtoList);

        // when
        List<DeomDto> result = deomService.getDeomPolicy(storeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).name()).isEqualTo(deomName);
        assertThat(result.get(0).requiredStampAmount()).isEqualTo(requiredStampAmount);

        verify(deomRepository).findPoliciesByStoreId(storeId);
    }

    @Test
    @DisplayName("디옴 정책 생성 성공")
    void createDeomPolicy_Success() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerId(eq(storeId), eq(userId)))
                    .thenReturn(Optional.of(store));
            when(deomRepository.existsByStoreIdAndName(eq(storeId), eq(deomName)))
                    .thenReturn(false);

            // 저장 후 반환되는 객체 설정
            Deom savedDeom = Deom.create(storeId, deomName, requiredStampAmount);
            // 리플렉션이나 다른 방법으로 id 설정이 필요하다면 여기서 처리
            when(deomRepository.save(any(Deom.class))).thenReturn(savedDeom);

            // when
            DeomResponse response = deomService.createDeomPolicy(createRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.storeId()).isEqualTo(storeId);
            assertThat(response.name()).isEqualTo(deomName);
            assertThat(response.requiredStampAmount()).isEqualTo(requiredStampAmount);

            verify(storeRepository).findByIdAndOwnerId(eq(storeId), eq(userId));
            verify(deomRepository).existsByStoreIdAndName(eq(storeId), eq(deomName));
            verify(deomRepository).save(any(Deom.class));
        }
    }

    @Test
    @DisplayName("디옴 정책 생성 실패 - 가게 소유권 없음")
    void createDeomPolicy_Fail_NoStoreOwnership() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerId(eq(storeId), eq(userId)))
                    .thenReturn(Optional.empty());

            // when & then
            DeomException exception =
                    assertThrows(
                            DeomException.class, () -> deomService.createDeomPolicy(createRequest));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.NO_PERMISSION_FOR_STORE);

            verify(storeRepository).findByIdAndOwnerId(eq(storeId), eq(userId));
            verify(deomRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("디옴 정책 생성 실패 - 이미 존재하는 정책")
    void createDeomPolicy_Fail_DuplicatePolicy() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerId(eq(storeId), eq(userId)))
                    .thenReturn(Optional.of(store));
            when(deomRepository.existsByStoreIdAndName(eq(storeId), eq(deomName))).thenReturn(true);

            // when & then
            DeomException exception =
                    assertThrows(
                            DeomException.class, () -> deomService.createDeomPolicy(createRequest));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.ALREADY_REGISTERED_DEOM);

            verify(storeRepository).findByIdAndOwnerId(eq(storeId), eq(userId));
            verify(deomRepository).existsByStoreIdAndName(eq(storeId), eq(deomName));
            verify(deomRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("디옴 정책 수정 성공")
    void updateDeomPolicy_Success() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerId(eq(storeId), eq(userId)))
                    .thenReturn(Optional.of(store));

            // 수정할 Deom 객체 생성
            Deom deomToUpdate = spy(Deom.create(storeId, deomName, requiredStampAmount));
            when(deomRepository.findById(eq(deomId))).thenReturn(Optional.of(deomToUpdate));

            // when
            DeomResponse response = deomService.updateDeomPolicy(deomId, updateRequest);

            // then
            assertThat(response).isNotNull();

            verify(storeRepository).findByIdAndOwnerId(eq(storeId), eq(userId));
            verify(deomRepository).findById(eq(deomId));
            verify(deomToUpdate)
                    .update(eq(updateRequest.name()), eq(updateRequest.requiredStampAmount()));
        }
    }

    @Test
    @DisplayName("디옴 정책 수정 실패 - 가게 소유권 없음")
    void updateDeomPolicy_Fail_NoStoreOwnership() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerId(eq(storeId), eq(userId)))
                    .thenReturn(Optional.empty());

            // when & then
            DeomException exception =
                    assertThrows(
                            DeomException.class,
                            () -> deomService.updateDeomPolicy(deomId, updateRequest));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.NO_PERMISSION_FOR_STORE);

            verify(storeRepository).findByIdAndOwnerId(eq(storeId), eq(userId));
            verify(deomRepository, never()).findById(any());
        }
    }

    @Test
    @DisplayName("디옴 정책 수정 실패 - 유효하지 않은 정책")
    void updateDeomPolicy_Fail_InvalidPolicy() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerId(eq(storeId), eq(userId)))
                    .thenReturn(Optional.of(store));
            when(deomRepository.findById(eq(deomId))).thenReturn(Optional.empty());

            // when & then
            DeomException exception =
                    assertThrows(
                            DeomException.class,
                            () -> deomService.updateDeomPolicy(deomId, updateRequest));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.INVALID_DEOM_POLICY);

            verify(storeRepository).findByIdAndOwnerId(eq(storeId), eq(userId));
            verify(deomRepository).findById(eq(deomId));
        }
    }

    @Test
    @DisplayName("디옴 정책 삭제 성공")
    void deleteDeomPolicy_Success() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerId(eq(storeId), eq(userId)))
                    .thenReturn(Optional.of(store));

            // 삭제할 Deom 객체 생성
            Deom deomToDelete = spy(Deom.create(storeId, deomName, requiredStampAmount));
            when(deomRepository.findById(eq(deomId))).thenReturn(Optional.of(deomToDelete));

            // when
            deomService.deleteDeomPolicy(deomId, storeId);

            // then
            verify(storeRepository).findByIdAndOwnerId(eq(storeId), eq(userId));
            verify(deomRepository).findById(eq(deomId));
            verify(deomToDelete).markAsDeleted();
        }
    }

    @Test
    @DisplayName("디옴 정책 삭제 실패 - 가게 소유권 없음")
    void deleteDeomPolicy_Fail_NoStoreOwnership() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerId(eq(storeId), eq(userId)))
                    .thenReturn(Optional.empty());

            // when & then
            DeomException exception =
                    assertThrows(
                            DeomException.class,
                            () -> deomService.deleteDeomPolicy(deomId, storeId));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.NO_PERMISSION_FOR_STORE);

            verify(storeRepository).findByIdAndOwnerId(eq(storeId), eq(userId));
            verify(deomRepository, never()).findById(any());
        }
    }

    @Test
    @DisplayName("디옴 정책 삭제 실패 - 유효하지 않은 정책")
    void deleteDeomPolicy_Fail_InvalidPolicy() {
        // given
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            when(storeRepository.findByIdAndOwnerId(eq(storeId), eq(userId)))
                    .thenReturn(Optional.of(store));
            when(deomRepository.findById(eq(deomId))).thenReturn(Optional.empty());

            // when & then
            DeomException exception =
                    assertThrows(
                            DeomException.class,
                            () -> deomService.deleteDeomPolicy(deomId, storeId));

            assertThat(exception.getBaseResponseCode())
                    .isEqualTo(CommonErrorCode.INVALID_DEOM_POLICY);

            verify(storeRepository).findByIdAndOwnerId(eq(storeId), eq(userId));
            verify(deomRepository).findById(eq(deomId));
        }
    }
}
