package kr.kro.deom.domain.stampPolicy.service;

import java.util.List;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.stampPolicy.dto.*;
import kr.kro.deom.domain.stampPolicy.entity.StampPolicy;
import kr.kro.deom.domain.stampPolicy.exception.StampPolicyException;
import kr.kro.deom.domain.stampPolicy.repository.StampPolicyRepository;
import kr.kro.deom.domain.store.exception.StoreException;
import kr.kro.deom.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StampPolicyService {
    private final StoreRepository storeRepository;
    private final StampPolicyRepository stampPolicyRepository;

    public List<StampPolicyDto> getStampPolicy(long storeId) {
        return stampPolicyRepository.findPoliciesByStoreId(storeId);
    }

    @Transactional
    public StampPolicyResponse createStampPolicy(StampPolicyRequest request) {

        validateStoreOwnership(request.storeId());

        checkDuplicatePolicy(request);

        StampPolicy stampPolicy =
                StampPolicy.create(request.storeId(), request.baseAmount(), request.stampCount());

        StampPolicy savedPolicy = stampPolicyRepository.save(stampPolicy);
        return StampPolicyResponse.from(savedPolicy);
    }

    @Transactional
    public StampPolicyResponse updateStampPolicy(Long policyId, StampPolicyUpdateRequest request) {

        validateStoreOwnership(request.storeId());

        StampPolicy stampPolicy = findAndValidStampPolicy(policyId);

        stampPolicy.update(request.baseAmount(), request.stampCount());

        return StampPolicyResponse.from(stampPolicy);
    }

    @Transactional
    public void deleteStampPolicy(Long policyId, Long storeId) {
        validateStoreOwnership(storeId);
        StampPolicy stampPolicy = findAndValidStampPolicy(policyId);
        stampPolicy.delete();
    }

    // 소유권 검증
    private void validateStoreOwnership(Long storeId) {
        storeRepository
                .findByIdAndOwnerIdAndIsDeletedFalse(storeId, SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new StoreException(CommonErrorCode.NO_PERMISSION_FOR_STORE));
    }

    // policy 중복 검증
    private void checkDuplicatePolicy(StampPolicyRequest request) {
        if (stampPolicyRepository.existsByStoreIdAndBaseAmountAndDeletedAtIsNull(
                request.storeId(), request.baseAmount())) {
            throw new StampPolicyException(CommonErrorCode.ALREADY_REGISTERED_STAMP_POLICY);
        }
    }

    // policy 조회
    private StampPolicy findAndValidStampPolicy(Long stampPolicyId) {
        return stampPolicyRepository
                .findByIdAndDeletedAtIsNull(stampPolicyId)
                .orElseThrow(() -> new StampPolicyException(CommonErrorCode.INVALID_STAMP_POLICY));
    }
}
