package kr.kro.deom.domain.stampPolicy.service;

import java.util.List;
import kr.kro.deom.domain.stampPolicy.dto.StampPolicyDto;
import kr.kro.deom.domain.stampPolicy.entity.StampPolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StampPolicyUpdateContext {
    private final List<StampPolicy> policiesToDelete;
    private final List<StampPolicyDto> policiesToCreate;
    private final List<StampPolicyUpdatePair> policiesToUpdate;

    @Getter
    @RequiredArgsConstructor
    public static class StampPolicyUpdatePair {
        private final StampPolicy existing;
        private final StampPolicyDto dto;
    }
}
