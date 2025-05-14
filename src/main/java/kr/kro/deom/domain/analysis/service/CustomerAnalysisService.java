package kr.kro.deom.domain.analysis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import kr.kro.deom.domain.analysis.dto.UserStampRankDto;
import kr.kro.deom.domain.exchange.dto.StampExchangeResponse;
import kr.kro.deom.domain.exchange.repository.StampExchangeRepository;
import kr.kro.deom.domain.exchange.service.StampExchangeJoinProjection;
import kr.kro.deom.domain.myStamp.dto.UserAccumulatedStampsDto;
import kr.kro.deom.domain.myStamp.service.MyStampService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerAnalysisService {
    private final MyStampService myStampService;
    private final StampExchangeRepository stampExchangeRepository;

    public List<UserStampRankDto> getCustomerRankingByAccumulatedStamp(Long storeId) {
        List<UserAccumulatedStampsDto> userAccumulatedStamps =
                myStampService.getUserAccumulatedStamps(storeId);

        if (userAccumulatedStamps.isEmpty()) {
            return List.of();
        }

        return makeUserStampRankDto(userAccumulatedStamps);
    }

    private List<UserStampRankDto> makeUserStampRankDto(
            List<UserAccumulatedStampsDto> userAccumulatedStamps) {
        List<UserStampRankDto> result = new ArrayList<>();
        int currentRank = 1;
        int previousAccumulated = -1;
        int sameRankCount = 1;

        for (UserAccumulatedStampsDto user : userAccumulatedStamps) {
            int accumulated = user.getAccumulatedStampAmount();

            if (accumulated == previousAccumulated) {
                sameRankCount++;
            } else {
                currentRank += sameRankCount - 1;
                if (previousAccumulated != -1) {
                    currentRank += 1;
                }
                sameRankCount = 1;
            }

            result.add(
                    UserStampRankDto.builder()
                            .userId(user.getUserId())
                            .accumulatedStampAmount(accumulated)
                            .rank(currentRank)
                            .build());

            previousAccumulated = accumulated;
        }

        return result;
    }

    public List<StampExchangeResponse> findMyStoreExchange(Long storeId) {
        return stampExchangeRepository.findAllExchanges(storeId).stream()
                .map(StampExchangeJoinProjection::toResponse)
                .collect(Collectors.toList());
    }
}
