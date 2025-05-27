package kr.kro.deom.domain.deom.service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.deom.dto.*;
import kr.kro.deom.domain.deom.entity.Deom;
import kr.kro.deom.domain.deom.exception.DeomException;
import kr.kro.deom.domain.deom.repository.DeomRepository;
import kr.kro.deom.domain.store.repository.StoreRepository;
import lombok.Getter;
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
        validateNoDuplicateNames(request.policies());

        List<Deom> existingPolicies = getExistingPolicies(storeId);
        DeomUpdateContext context = createUpdateContext(storeId, request, existingPolicies);

        markPoliciesForDeletion(context.getPoliciesToDelete());
        List<Deom> updatedPolicies = processDeomPolicies(storeId, context);

        return createResponse(updatedPolicies);
    }

    @Getter
    private static class DeomUpdateContext {
        private final List<Deom> policiesToDelete;
        private final List<DeomDto> policiesToCreate;
        private final List<DeomUpdatePair> policiesToUpdate;

        public DeomUpdateContext(
                List<Deom> policiesToDelete,
                List<DeomDto> policiesToCreate,
                List<DeomUpdatePair> policiesToUpdate) {
            this.policiesToDelete = policiesToDelete;
            this.policiesToCreate = policiesToCreate;
            this.policiesToUpdate = policiesToUpdate;
        }

        @Getter
        private static class DeomUpdatePair {
            private final Deom existing;
            private final DeomDto dto;

            public DeomUpdatePair(Deom existing, DeomDto dto) {
                this.existing = existing;
                this.dto = dto;
            }
        }
    }

    private List<Deom> getExistingPolicies(Long storeId) {
        return deomRepository.findByStoreIdAndDeletedAtIsNull(storeId);
    }

    private DeomUpdateContext createUpdateContext(
            Long storeId, DeomsRequest request, List<Deom> existingPolicies) {

        Map<Long, Deom> existingPolicyMap =
                existingPolicies.stream()
                        .collect(Collectors.toMap(Deom::getId, Function.identity()));

        Set<String> existingNames =
                existingPolicies.stream().map(Deom::getName).collect(Collectors.toSet());

        List<Deom> policiesToDelete = new ArrayList<>();
        List<DeomDto> policiesToCreate = new ArrayList<>();
        List<DeomUpdateContext.DeomUpdatePair> policiesToUpdate = new ArrayList<>();

        Set<Long> requestedIds = new HashSet<>();

        for (DeomDto dto : request.policies()) {
            if (dto.id() == null) {
                validateNewPolicyName(storeId, dto.name(), existingNames);
                policiesToCreate.add(dto);
            } else {
                requestedIds.add(dto.id());
                Deom existingPolicy = existingPolicyMap.get(dto.id());
                if (existingPolicy == null) {
                    throw new DeomException(CommonErrorCode.INVALID_DEOM_POLICY);
                }
                policiesToUpdate.add(new DeomUpdateContext.DeomUpdatePair(existingPolicy, dto));
            }
        }

        for (Deom existing : existingPolicies) {
            if (!requestedIds.contains(existing.getId())) {
                policiesToDelete.add(existing);
            }
        }

        return new DeomUpdateContext(policiesToDelete, policiesToCreate, policiesToUpdate);
    }

    private void validateNewPolicyName(Long storeId, String name, Set<String> existingNames) {
        if (existingNames.contains(name)) {
            throw new DeomException(CommonErrorCode.ALREADY_REGISTERED_DEOM);
        }

        if (deomRepository.existsByStoreIdAndNameAndDeletedAtIsNull(storeId, name)) {
            throw new DeomException(CommonErrorCode.ALREADY_REGISTERED_DEOM);
        }
    }

    private void markPoliciesForDeletion(List<Deom> policies) {
        policies.forEach(Deom::markAsDeleted);
    }

    private List<Deom> processDeomPolicies(Long storeId, DeomUpdateContext context) {
        List<Deom> result = new ArrayList<>();

        if (!context.getPoliciesToCreate().isEmpty()) {
            List<Deom> newPolicies =
                    context.getPoliciesToCreate().stream()
                            .map(dto -> Deom.create(storeId, dto.name(), dto.requiredStampAmount()))
                            .toList();

            List<Deom> savedNewPolicies = deomRepository.saveAll(newPolicies);
            result.addAll(savedNewPolicies);
        }

        context.getPoliciesToUpdate().forEach(this::updateExistingPolicy);

        context.getPoliciesToUpdate().stream()
                .map(DeomUpdateContext.DeomUpdatePair::getExisting)
                .forEach(result::add);

        return result;
    }

    private void updateExistingPolicy(DeomUpdateContext.DeomUpdatePair pair) {
        pair.getExisting().update(pair.getDto().name(), pair.getDto().requiredStampAmount());
    }

    private DeomsResponse createResponse(List<Deom> policies) {
        List<DeomDto> results = policies.stream().map(this::convertToDto).toList();
        return new DeomsResponse(results);
    }

    private DeomDto convertToDto(Deom policy) {
        return new DeomDto(policy.getId(), policy.getName(), policy.getRequiredStampAmount());
    }

    private void validateNoDuplicateNames(List<DeomDto> policies) {
        Set<String> names = new HashSet<>();
        for (DeomDto policy : policies) {
            if (!names.add(policy.name())) {
                throw new DeomException(CommonErrorCode.ALREADY_REGISTERED_DEOM);
            }
        }
    }

    private void validateStoreOwnership(Long storeId) {
        storeRepository
                .findByIdAndOwnerId(storeId, SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new DeomException(CommonErrorCode.NO_PERMISSION_FOR_STORE));
    }

    public Deom getDeom(Long deomId) {
        return deomRepository
                .findById(deomId)
                .orElseThrow(() -> new DeomException(CommonErrorCode.DEOM_NOT_FOUND));
    }
}
