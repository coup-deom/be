package kr.kro.deom.domain.stampPolicy.service;

import java.util.*;
import java.util.function.Function;
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
    public StampPoliciesResponse updateAllStampPolicies(
            Long storeId, StampPoliciesRequest request) {
        validateStoreOwnership(storeId);
        validateNoDuplicateBaseAmounts(request.policies());

        List<StampPolicy> existingPolicies = getExistingPolicies(storeId);
        StampPolicyUpdateContext context = createUpdateContext(request, existingPolicies);

        markPoliciesForDeletion(context.getPoliciesToDelete());
        List<StampPolicy> updatedPolicies = processStampPolicies(storeId, context);

        return createResponse(updatedPolicies);
    }

    private List<StampPolicy> getExistingPolicies(Long storeId) {
        return stampPolicyRepository.findByStoreIdAndDeletedAtIsNull(storeId);
    }

    private StampPolicyUpdateContext createUpdateContext(
            StampPoliciesRequest request, List<StampPolicy> existingPolicies) {

        Map<Long, StampPolicy> existingPolicyMap =
                existingPolicies.stream()
                        .collect(Collectors.toMap(StampPolicy::getId, Function.identity()));

        List<StampPolicy> policiesToDelete = new ArrayList<>();
        List<StampPolicyDto> policiesToCreate = new ArrayList<>();
        List<StampPolicyUpdateContext.StampPolicyUpdatePair> policiesToUpdate = new ArrayList<>();

        Set<Long> requestedIds = new HashSet<>();

        for (StampPolicyDto dto : request.policies()) {
            if (dto.id() == null) {
                policiesToCreate.add(dto);
            } else {
                requestedIds.add(dto.id());
                StampPolicy existingPolicy = existingPolicyMap.get(dto.id());
                if (existingPolicy == null) {
                    throw new StampPolicyException(CommonErrorCode.INVALID_STAMP_POLICY);
                }
                policiesToUpdate.add(
                        new StampPolicyUpdateContext.StampPolicyUpdatePair(existingPolicy, dto));
            }
        }

        for (StampPolicy existing : existingPolicies) {
            if (!requestedIds.contains(existing.getId())) {
                policiesToDelete.add(existing);
            }
        }

        return new StampPolicyUpdateContext(policiesToDelete, policiesToCreate, policiesToUpdate);
    }

    private void markPoliciesForDeletion(List<StampPolicy> policies) {
        policies.forEach(StampPolicy::markAsDeleted);
    }

    private List<StampPolicy> processStampPolicies(Long storeId, StampPolicyUpdateContext context) {
        List<StampPolicy> result = new ArrayList<>();

        if (!context.getPoliciesToCreate().isEmpty()) {
            List<StampPolicy> newPolicies =
                    context.getPoliciesToCreate().stream()
                            .map(
                                    dto ->
                                            StampPolicy.create(
                                                    storeId, dto.baseAmount(), dto.stampCount()))
                            .toList();

            List<StampPolicy> savedNewPolicies = stampPolicyRepository.saveAll(newPolicies);
            result.addAll(savedNewPolicies);
        }

        List<StampPolicy> updatedPolicies =
                context.getPoliciesToUpdate().stream().map(this::updateExistingPolicy).toList();
        result.addAll(updatedPolicies);

        return result;
    }

    private StampPolicy updateExistingPolicy(StampPolicyUpdateContext.StampPolicyUpdatePair pair) {
        pair.getExisting().update(pair.getDto().baseAmount(), pair.getDto().stampCount());
        return pair.getExisting();
    }

    private StampPoliciesResponse createResponse(List<StampPolicy> policies) {
        List<StampPolicyDto> results = policies.stream().map(this::convertToDto).toList();
        return new StampPoliciesResponse(results);
    }

    private StampPolicyDto convertToDto(StampPolicy policy) {
        return new StampPolicyDto(policy.getId(), policy.getBaseAmount(), policy.getStampCount());
    }

    private void validateNoDuplicateBaseAmounts(List<StampPolicyDto> policies) {
        Set<Integer> baseAmounts = new HashSet<>();
        for (StampPolicyDto policy : policies) {
            if (!baseAmounts.add(policy.baseAmount())) {
                throw new StampPolicyException(CommonErrorCode.ALREADY_REGISTERED_STAMP_POLICY);
            }
        }
    }

    private void validateStoreOwnership(Long storeId) {
        storeRepository
                .findByIdAndOwnerIdAndIsDeletedFalse(storeId, SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new StoreException(CommonErrorCode.NO_PERMISSION_FOR_STORE));
    }
}
