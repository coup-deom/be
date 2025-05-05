package kr.kro.deom.domain.myStamp.service;

import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyStampService {

    private final MyStampRepository myStampRepository;

    public Integer getMyStampAmount(Long userId, Long storeId) {
        return myStampRepository.findStampAmountByUserIdAndStoreId(userId, storeId);
    }
}
