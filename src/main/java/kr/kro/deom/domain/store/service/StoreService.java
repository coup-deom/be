package kr.kro.deom.domain.store.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.time.Instant;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.store.dto.request.StoreRegisterRequest;
import kr.kro.deom.domain.store.dto.response.StoreRegisterResponse;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.exception.StoreException;
import kr.kro.deom.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Tag(name = "store API", description = "")
public class StoreService {

    private final StoreRepository storeRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public StoreRegisterResponse registerStore(StoreRegisterRequest request) {
        validateDuplicateStore(request);

        Instant now = Instant.now();
        Instant farFuture = now.plus(Duration.ofDays(365 * 100));

        Store store =
                Store.builder()
                        .ownerId(request.getOwnerId())
                        .businessNumber(request.getBusinessNumber())
                        .storeName(request.getStoreName())
                        .branchName(request.getBranchName())
                        .addressCity(request.getAddressCity())
                        .addressStreet(request.getAddressStreet())
                        .addressDetail(request.getAddressDetail())
                        .createdAt(now)
                        .updatedAt(now)
                        .deletedAt(farFuture)
                        .isDeleted(false)
                        .image(request.getImage())
                        .build();

        Store savedStore = storeRepository.save(store);
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

    private StoreRegisterResponse mapToRegisterResponse(Store store) {
        return objectMapper.convertValue(store, StoreRegisterResponse.class);
    }
}
