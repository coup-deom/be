package kr.kro.deom.domain.deom.service;

import java.util.List;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.deom.dto.DeomDto;
import kr.kro.deom.domain.deom.dto.DeomRequest;
import kr.kro.deom.domain.deom.dto.DeomResponse;
import kr.kro.deom.domain.deom.dto.DeomUpdateRequest;
import kr.kro.deom.domain.deom.entity.Deom;
import kr.kro.deom.domain.deom.repository.DeomRepository;
import kr.kro.deom.domain.exception.DeomException;
import kr.kro.deom.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeomService {

    private final DeomRepository deomRepository;
    private final StoreRepository storeRepository;

    public List<DeomDto> getDeomPolicy(long storeId) {
        return deomRepository.findPoliciesByStoreId(storeId);
    }

    @Transactional
    public DeomResponse createDeomPolicy(DeomRequest request) {

        validateStoreOwnership(request.storeId());

        checkDuplicatePolicy(request);

        Deom deom = Deom.create(request.storeId(), request.name(), request.requiredStampAmount());

        Deom savedDeom = deomRepository.save(deom);
        return DeomResponse.from(savedDeom);
    }

    @Transactional
    public DeomResponse updateDeomPolicy(Long deomId, DeomUpdateRequest request) {

        validateStoreOwnership(request.storeId());

        Deom deom = findAndValidDeomPolicy(deomId);

        deom.update(request.name(), request.requiredStampAmount());

        return DeomResponse.from(deom);
    }

    @Transactional
    public void deleteDeomPolicy(Long deomId, Long storeId) {
        validateStoreOwnership(storeId);
        Deom deom = findAndValidDeomPolicy(deomId);
        deom.delete();
    }

    // 소유권 검증
    private void validateStoreOwnership(Long storeId) {
        storeRepository
                .findByIdAndOwnerId(storeId, SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new DeomException(CommonErrorCode.NO_PERMISSION_FOR_STORE));
    }

    // policy 중복 검증
    private void checkDuplicatePolicy(DeomRequest request) {
        if (deomRepository.existsByStoreIdAndName(request.storeId(), request.name())) {
            throw new DeomException(CommonErrorCode.ALREADY_REGISTERED_DEOM);
        }
    }

    // policy 조회
    private Deom findAndValidDeomPolicy(Long deom) {
        return deomRepository
                .findById(deom)
                .orElseThrow(() -> new DeomException(CommonErrorCode.INVALID_DEOM_POLICY));
    }
}
