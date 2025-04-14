package kr.kro.deom.domain.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.store.dto.request.StoreRegisterRequest;
import kr.kro.deom.domain.store.dto.response.StoreRegisterResponse;
import kr.kro.deom.domain.store.entity.Status;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.exception.StoreException;
import kr.kro.deom.domain.store.repository.StoreRepository;
import kr.kro.deom.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock private UserService userService;

    @InjectMocks private StoreService storeService;

    private StoreRegisterRequest request;
    private Store savedStore;
    private StoreRegisterResponse response;

    @BeforeEach
    void setUp() {
        // 테스트 요청 객체 설정
        request = new StoreRegisterRequest();
        request.setOwnerId(1L);
        request.setBusinessNumber(1234567890L);
        request.setStoreName("테스트 상점");
        request.setBranchName("테스트 지점");
        request.setAddressCity("서울시");
        request.setAddressStreet("강남구 테헤란로");
        request.setAddressDetail("123번지");
        request.setImage("store_image.jpg");

        // 저장된 상점 객체 설정
        savedStore = new Store();
        savedStore.setId(1L);
        savedStore.setOwnerId(request.getOwnerId());
        savedStore.setBusinessNumber(request.getBusinessNumber());
        savedStore.setStoreName(request.getStoreName());
        savedStore.setBranchName(request.getBranchName());
        savedStore.setAddressCity(request.getAddressCity());
        savedStore.setAddressStreet(request.getAddressStreet());
        savedStore.setAddressDetail(request.getAddressDetail());
        savedStore.setIsDeleted(false);
        savedStore.setImage(request.getImage());
        savedStore.setStatus(Status.APPROVED);

        // 응답 객체 설정
        response = new StoreRegisterResponse();
        response.setOwnerId(savedStore.getOwnerId());
        response.setBusinessNumber(savedStore.getBusinessNumber());
        response.setStoreName(savedStore.getStoreName());
        response.setBranchName(savedStore.getBranchName());
        response.setAddressCity(savedStore.getAddressCity());
        response.setAddressStreet(savedStore.getAddressStreet());
        response.setAddressDetail(savedStore.getAddressDetail());
        response.setImage(savedStore.getImage());
    }

    @Test
    @DisplayName("상점 등록 성공 테스트")
    void registerStore_Success() {
        // Given
        doNothing().when(userService).validateUserByUserId(anyLong());
        when(storeRepository.findByBusinessNumberAndIsDeletedFalse(anyLong()))
                .thenReturn(Optional.empty());
        when(storeRepository.findByStoreNameAndBranchNameAndIsDeletedFalse(
                        anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(storeRepository.save(any(Store.class))).thenReturn(savedStore);
        when(objectMapper.convertValue(any(), eq(StoreRegisterResponse.class)))
                .thenReturn(response);

        // When
        StoreRegisterResponse result = storeService.registerStore(request);

        // Then
        assertNotNull(result);
        assertEquals(response.getOwnerId(), result.getOwnerId());
        assertEquals(response.getBusinessNumber(), result.getBusinessNumber());
        assertEquals(response.getStoreName(), result.getStoreName());

        // Verify
        verify(userService).validateUserByUserId(request.getOwnerId());
        verify(storeRepository).findByBusinessNumberAndIsDeletedFalse(request.getBusinessNumber());
        verify(storeRepository)
                .findByStoreNameAndBranchNameAndIsDeletedFalse(
                        request.getStoreName(), request.getBranchName());
        verify(storeRepository).save(any(Store.class));
        verify(objectMapper).convertValue(any(), eq(StoreRegisterResponse.class));
    }

    @Test
    @DisplayName("사업자 번호 중복 시 예외 발생 테스트")
    void registerStore_DuplicateBusinessNumber_ThrowsException() {
        // Given
        doNothing().when(userService).validateUserByUserId(anyLong());
        when(storeRepository.findByBusinessNumberAndIsDeletedFalse(anyLong()))
                .thenReturn(Optional.of(savedStore));

        // When & Then
        StoreException exception =
                assertThrows(StoreException.class, () -> storeService.registerStore(request));

        assertEquals(CommonErrorCode.DUPLICATE_BUSINESS_NUMBER, exception.getBaseResponseCode());

        // Verify
        verify(userService).validateUserByUserId(request.getOwnerId());
        verify(storeRepository).findByBusinessNumberAndIsDeletedFalse(request.getBusinessNumber());
        verify(storeRepository, never())
                .findByStoreNameAndBranchNameAndIsDeletedFalse(anyString(), anyString());
        verify(storeRepository, never()).save(any(Store.class));
    }

    @Test
    @DisplayName("상점명과 지점명 중복 시 예외 발생 테스트")
    void registerStore_DuplicateStoreNameAndBranch_ThrowsException() {
        // Given
        doNothing().when(userService).validateUserByUserId(anyLong());
        when(storeRepository.findByBusinessNumberAndIsDeletedFalse(anyLong()))
                .thenReturn(Optional.empty());
        when(storeRepository.findByStoreNameAndBranchNameAndIsDeletedFalse(
                        anyString(), anyString()))
                .thenReturn(Optional.of(savedStore));

        // When & Then
        StoreException exception =
                assertThrows(StoreException.class, () -> storeService.registerStore(request));

        assertEquals(
                CommonErrorCode.DUPLICATE_STORE_NAME_AND_BRANCH, exception.getBaseResponseCode());

        // Verify
        verify(userService).validateUserByUserId(request.getOwnerId());
        verify(storeRepository).findByBusinessNumberAndIsDeletedFalse(request.getBusinessNumber());
        verify(storeRepository)
                .findByStoreNameAndBranchNameAndIsDeletedFalse(
                        request.getStoreName(), request.getBranchName());
        verify(storeRepository, never()).save(any(Store.class));
    }
}
