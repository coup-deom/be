package kr.kro.deom.domain.analysis.service;

import java.util.List;
import kr.kro.deom.domain.myStamp.dto.UserAccumulatedStampsDto;
import kr.kro.deom.domain.myStamp.service.MyStampService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerAnalysisService {
    private MyStampService myStampService;

    public List<UserAccumulatedStampsDto> getCustomerRankingByAccumulatedStamp(Long storeId) {
        return myStampService.getUserAccumulatedStamps(storeId);
    }
}
