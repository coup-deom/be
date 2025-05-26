package kr.kro.deom.domain.deom.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.deom.dto.*;
import kr.kro.deom.domain.deom.entity.Deom;
import kr.kro.deom.domain.deom.exception.DeomException;
import kr.kro.deom.domain.deom.repository.DeomRepository;
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
        return deomRepository.findPoliciesByStoreIdAndDeletedAtIsNull(storeId);
    }

    @Transactional
    public DeomsResponse updateAllDeomPolicies(Long storeId, DeomsRequest request) {

        validateStoreOwnership(storeId);
        List<Deom> existingPolicies = deomRepository.findByStoreIdAndDeletedAtIsNull(storeId);
        validateNoDuplicateNames(request.policies());

        List<Long> retainedPolicyIds =
                request.policies().stream().map(DeomDto::id).filter(id -> id != null).toList();


        existingPolicies.stream()
                .filter(policy -> !retainedPolicyIds.contains(policy.getId()))
                .forEach(Deom::markAsDeleted);

        List<Deom> updatedPolicies = new ArrayList<>();

        for (DeomDto policyDto : request.policies()) {
            if (policyDto.id() == null) {

                checkDuplicatePolicy(storeId, policyDto.name());
                Deom newPolicy =
                        Deom.create(storeId, policyDto.name(), policyDto.requiredStampAmount());
                updatedPolicies.add(deomRepository.save(newPolicy));
            } else {

                Deom existingPolicy =
                        existingPolicies.stream()
                                .filter(p -> p.getId().equals(policyDto.id()))
                                .findFirst()
                                .orElseThrow(
                                        () ->
                                                new DeomException(
                                                        CommonErrorCode.INVALID_DEOM_POLICY));
                existingPolicy.update(policyDto.name(), policyDto.requiredStampAmount());
                updatedPolicies.add(existingPolicy);
            }
        }

        List<DeomDto> results =
                updatedPolicies.stream()
                        .map(p -> new DeomDto(p.getId(), p.getName(), p.getRequiredStampAmount()))
                        .collect(Collectors.toList());

        return new DeomsResponse(results);
    }


    private void validateNoDuplicateNames(List<DeomDto> policies) {
        List<String> names = policies.stream().map(DeomDto::name).collect(Collectors.toList());

        Set<String> uniqueNames = new HashSet<>(names);
        if (names.size() != uniqueNames.size()) {
            throw new DeomException(CommonErrorCode.ALREADY_REGISTERED_DEOM); // 기존 에러코드 재사용
        }
    }

    @Transactional
    public DeomResponse createDeomPolicy(DeomRequest request) {

        validateStoreOwnership(request.storeId());

        checkDuplicatePolicy(request.storeId(), request.name());

        Deom deom = Deom.create(request.storeId(), request.name(), request.requiredStampAmount());

        Deom savedDeom = deomRepository.save(deom);
        return DeomResponse.from(savedDeom);
    }

    @Transactional
    public DeomResponse updateDeomPolicy(Long deomId, DeomUpdateRequest request) {

        validateStoreOwnership(request.storeId());

        Deom deom = getValidDeomById(deomId);

        deom.update(request.name(), request.requiredStampAmount());

        return DeomResponse.from(deom);
    }

    @Transactional
    public void deleteDeomPolicy(Long deomId, Long storeId) {
        validateStoreOwnership(storeId);
        Deom deom = getValidDeomById(deomId);
        deom.markAsDeleted();
    }

    // 소유권 검증
    private void validateStoreOwnership(Long storeId) {
        storeRepository
                .findByIdAndOwnerId(storeId, SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new DeomException(CommonErrorCode.NO_PERMISSION_FOR_STORE));
    }


    private void checkDuplicatePolicy(Long storeId, String name) {
        if (deomRepository.existsByStoreIdAndName(storeId, name)) {
            throw new DeomException(CommonErrorCode.ALREADY_REGISTERED_DEOM);
        }
    }


    private Deom getValidDeomById(Long deomId) {
        return deomRepository
                .findById(deomId)
                .orElseThrow(() -> new DeomException(CommonErrorCode.INVALID_DEOM_POLICY));
    }

    public Deom getDeom(Long deomId) {
        return deomRepository
                .findById(deomId)
                .orElseThrow(() -> new DeomException(CommonErrorCode.DEOM_NOT_FOUND));
    }
}
