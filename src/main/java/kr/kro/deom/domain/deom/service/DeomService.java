package kr.kro.deom.domain.deom.service;

import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.deom.entity.Deom;
import kr.kro.deom.domain.deom.exception.DeomException;
import kr.kro.deom.domain.deom.repository.DeomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeomService {

    private final DeomRepository deomRepository;

    public Deom getDeom(Long deomId) {
        return deomRepository.findById(deomId).orElseThrow(() -> new DeomException(CommonErrorCode.DEOM_NOT_FOUND));
    }
}
