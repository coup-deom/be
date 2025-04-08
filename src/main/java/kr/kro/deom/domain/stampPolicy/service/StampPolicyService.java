package kr.kro.deom.domain.stampPolicy.service;

import java.util.List;
import kr.kro.deom.domain.stampPolicy.dto.StampPolicyDto;
import kr.kro.deom.domain.stampPolicy.repository.StampPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StampPolicyService {
    private final StampPolicyRepository stampPolicyRepository;

    public List<StampPolicyDto> getStampPolicy(long storeId) {
        return stampPolicyRepository.findPoliciesByStoreId(storeId);
    }
}
