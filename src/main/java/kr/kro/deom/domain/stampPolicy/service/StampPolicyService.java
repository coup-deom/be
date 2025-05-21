package kr.kro.deom.domain.stampPolicy.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public StampPoliciesResponse updateAllStampPolicies(Long storeId, StampPoliciesRequest request) {

        validateStoreOwnership(storeId);
        List<StampPolicy> existingPolicies = stampPolicyRepository.findByStoreIdAndDeletedAtIsNull(storeId);
        velidateNoDuplicateBaseAmounts(request.policies());

        List<Long> newPolicyIds = request.policies().stream()
                .filter(p-> p.id() !=  null)
                .map(StampPolicyDto::id)
                .collect(Collectors.toList());

        existingPolicies.stream()
                .filter(policy -> !newPolicyIds.contains(policy.getId()))
                .forEach(StampPolicy::markAsDeleted);

        List<StampPolicy> updatePolicies = new ArrayList<>();

        for(StampPolicyDto policyDto : request.policies()) {
            if(policyDto.id() == null) {
                StampPolicy newPolicy = StampPolicy.create(storeId, policyDto.baseAmount(),policyDto.stampCount());
                updatePolicies.add(stampPolicyRepository.save(newPolicy));
            }else{
                StampPolicy existingPolicy = existingPolicies.stream()
                        .filter(p -> p.getId().equals(policyDto.id()))
                        .findFirst()
                        .orElseThrow(()-> new StampPolicyException(CommonErrorCode.INVALID_STAMP_POLICY));
                existingPolicy.update(policyDto.baseAmount(), policyDto.stampCount());
                updatePolicies.add(existingPolicy);
            }
        }

        List<StampPolicyDto> results = updatePolicies.stream()
                .map(p -> new StampPolicyDto(p.getId(), p.getBaseAmount(), p.getStampCount()))
                .collect(Collectors.toList());

        return new StampPoliciesResponse(results);

    }

    private void velidateNoDuplicateBaseAmounts(List<StampPolicyDto> policies) {
        Set<Integer> baseAmounts = new HashSet<>();
        for(StampPolicyDto policy : policies) {
            if(!baseAmounts.add(policy.baseAmount())){
                throw new StampPolicyException(CommonErrorCode.ALREADY_REGISTERED_STAMP_POLICY);
            }

        }
    }

    @Transactional
    public StampPolicyResponse createStampPolicy(StampPolicyRequest request) {

        validateStoreOwnership(request.storeId());

        checkDuplicatePolicy(request.storeId(), request.baseAmount());

        StampPolicy stampPolicy =
                StampPolicy.create(request.storeId(), request.baseAmount(), request.stampCount());

        StampPolicy savedPolicy = stampPolicyRepository.save(stampPolicy);
        return StampPolicyResponse.from(savedPolicy);
    }

    @Transactional
    public StampPolicyResponse updateStampPolicy(Long policyId, StampPolicyUpdateRequest request) {

        validateStoreOwnership(request.storeId());

        StampPolicy stampPolicy = getValidStampPolicyById(policyId);

        stampPolicy.update(request.baseAmount(), request.stampCount());

        return StampPolicyResponse.from(stampPolicy);
    }

    @Transactional
    public void deleteStampPolicy(Long policyId, Long storeId) {
        validateStoreOwnership(storeId);
        StampPolicy stampPolicy = getValidStampPolicyById(policyId);
        stampPolicy.markAsDeleted();
    }

    // 소유권 검증
    private void validateStoreOwnership(Long storeId) {
        storeRepository
                .findByIdAndOwnerIdAndIsDeletedFalse(storeId, SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new StoreException(CommonErrorCode.NO_PERMISSION_FOR_STORE));
    }

    // policy 중복 검증
    private void checkDuplicatePolicy(Long storeId, int baseAmount) {
        if (stampPolicyRepository.existsByStoreIdAndBaseAmountAndDeletedAtIsNull(
                storeId, baseAmount)) {
            throw new StampPolicyException(CommonErrorCode.ALREADY_REGISTERED_STAMP_POLICY);
        }
    }

    // policy 조회
    private StampPolicy getValidStampPolicyById(Long stampPolicyId) {
        return stampPolicyRepository
                .findByIdAndDeletedAtIsNull(stampPolicyId)
                .orElseThrow(() -> new StampPolicyException(CommonErrorCode.INVALID_STAMP_POLICY));
    }
}
