package kr.kro.deom.domain.store.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.store.dto.request.StoreRegisterRequest;
import kr.kro.deom.domain.store.dto.response.StoreRegisterResponse;
import kr.kro.deom.domain.store.entity.Status;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.exception.StoreException;
import kr.kro.deom.domain.store.repository.StoreRepository;
import kr.kro.deom.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Tag(name = "store API", description = "")
public class StoreService {

    private final StoreRepository storeRepository;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Transactional
    public StoreRegisterResponse registerStore(StoreRegisterRequest request) {

        userService.validateUserByUserId(request.getOwnerId());
        validateDuplicateStore(request);

        Store store =
                Store.builder()
                        .ownerId(request.getOwnerId())
                        .businessNumber(request.getBusinessNumber())
                        .storeName(request.getStoreName())
                        .branchName(request.getBranchName())
                        .addressCity(request.getAddressCity())
                        .addressStreet(request.getAddressStreet())
                        .addressDetail(request.getAddressDetail())
                        .isDeleted(false)
                        .image(request.getImage())
                        .status(Status.PENDING)
                        .build();

        Store savedStore = storeRepository.save(store);
        savedStore.setStatus(Status.APPROVED); // 일단 무조건 승인
        return mapToRegisterResponse(savedStore);
    }

    private void validateDuplicateStore(StoreRegisterRequest request) {
        storeRepository
                .findByBusinessNumberAndIsDeletedFalse(request.getBusinessNumber())
                .ifPresent(
                        store -> {
                            throw new StoreException(CommonErrorCode.DUPLICATE_BUSINESS_NUMBER);
                        });

        storeRepository
                .findByStoreNameAndBranchNameAndIsDeletedFalse(
                        request.getStoreName(), request.getBranchName())
                .ifPresent(
                        store -> {
                            throw new StoreException(
                                    CommonErrorCode.DUPLICATE_STORE_NAME_AND_BRANCH);
                        });
    }

    StoreRegisterResponse mapToRegisterResponse(Store store) {
        return objectMapper.convertValue(store, StoreRegisterResponse.class);
    }

    public Store getStore(Long storeId) {
        return storeRepository.findById(storeId).orElseThrow(() -> new StoreException(CommonErrorCode.STORE_NOT_FOUND));
    }
}
