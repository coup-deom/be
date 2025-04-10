package kr.kro.deom.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Optional;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.store.dto.request.StoreRegisterRequest;
import kr.kro.deom.domain.store.dto.response.StoreRegisterResponse;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.exception.StoreException;
import kr.kro.deom.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock private StoreRepository storeRepository;

    @Mock private ObjectMapper objectMapper;

    @InjectMocks private StoreService storeService;

    @Test
    @DisplayName("사업자번호 중복 시 예외 발생")
    void registerStore_whenDuplicateBusinessNumber_thenThrowException() {
        // given
        StoreRegisterRequest request = createSampleRequest();

        when(storeRepository.findByBusinessNumberAndIsDeletedFalse(request.getBusinessNumber()))
                .thenReturn(Optional.of(Store.builder().build()));

        // when & then
        StoreException exception =
                assertThrows(StoreException.class, () -> storeService.registerStore(request));

        assertThat(exception.getBaseResponseCode())
                .isEqualTo(CommonErrorCode.DUPLICATE_BUSINESS_NUMBER);
    }

    @Test
    @DisplayName("가게명과 지점명 중복 시 예외 발생")
    void registerStore_whenDuplicateStoreNameAndBranch_thenThrowException() {
        // given
        StoreRegisterRequest request = createSampleRequest();

        when(storeRepository.findByBusinessNumberAndIsDeletedFalse(request.getBusinessNumber()))
                .thenReturn(Optional.empty());

        when(storeRepository.findByStoreNameAndBranchNameAndIsDeletedFalse(
                        request.getStoreName(), request.getBranchName()))
                .thenReturn(Optional.of(Store.builder().build()));

        // when & then
        StoreException exception =
                assertThrows(StoreException.class, () -> storeService.registerStore(request));

        assertThat(exception.getBaseResponseCode())
                .isEqualTo(CommonErrorCode.DUPLICATE_STORE_NAME_AND_BRANCH);
    }

    @Test
    @DisplayName("가게 등록 성공")
    void registerStore_whenValid_thenSuccess() {
        // given
        StoreRegisterRequest request = createSampleRequest();

        when(storeRepository.findByBusinessNumberAndIsDeletedFalse(request.getBusinessNumber()))
                .thenReturn(Optional.empty());

        when(storeRepository.findByStoreNameAndBranchNameAndIsDeletedFalse(
                        request.getStoreName(), request.getBranchName()))
                .thenReturn(Optional.empty());

        Store savedStore =
                Store.builder()
                        .id(1L)
                        .ownerId(request.getOwnerId())
                        .businessNumber(request.getBusinessNumber())
                        .storeName(request.getStoreName())
                        .branchName(request.getBranchName())
                        .addressCity(request.getAddressCity())
                        .addressStreet(request.getAddressStreet())
                        .addressDetail(request.getAddressDetail())
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .isDeleted(false)
                        .image(request.getImage())
                        .build();

        when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

        StoreRegisterResponse expectedResponse = new StoreRegisterResponse();
        expectedResponse.setOwnerId(1L);
        expectedResponse.setOwnerId(request.getOwnerId());
        expectedResponse.setBusinessNumber(request.getBusinessNumber());
        expectedResponse.setStoreName(request.getStoreName());

        when(objectMapper.convertValue(savedStore, StoreRegisterResponse.class))
                .thenReturn(expectedResponse);

        // when
        StoreRegisterResponse response = storeService.registerStore(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOwnerId()).isEqualTo(1L);
        assertThat(response.getOwnerId()).isEqualTo(request.getOwnerId());
        assertThat(response.getBusinessNumber()).isEqualTo(request.getBusinessNumber());
        assertThat(response.getStoreName()).isEqualTo(request.getStoreName());
    }

    private StoreRegisterRequest createSampleRequest() {
        return StoreRegisterRequest.builder()
                .ownerId(1L)
                .storeName("테스트가게")
                .branchName("본점")
                .businessNumber(1234567890L)
                .addressCity("서울시")
                .addressStreet("종로구")
                .addressDetail("1번지")
                .image("image.jpg")
                .build();
    }
}
