package kr.kro.deom.domain.analysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.analysis.dto.UserStampRankDto;
import kr.kro.deom.domain.analysis.service.CustomerAnalysisService;
import kr.kro.deom.domain.exchange.dto.StampExchangeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
@Tag(name = "analysis", description = "단골 고객 분석 API")
public class CustomerAnalysisController {

    private final CustomerAnalysisService customerAnalysisService;

    @Operation(summary = "누적 스탬프 고객 순위 조회", description = "누적 적립 스탬프가 많은 순서로 고객을 조회합니다.")
    @GetMapping("/customer")
    public ResponseEntity<ApiResponse<List<UserStampRankDto>>> getCustomerRanking(
            @RequestParam Long storeId) {
        List<UserStampRankDto> response =
                customerAnalysisService.getCustomerRankingByAccumulatedStamp(storeId);

        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @Operation(summary = "내 가게의 스탬프 거래 조회 ", description = "내 가게의 스탬프 거래를 조회합니다.")
    @GetMapping("/recent-coupons")
    public ResponseEntity<ApiResponse<List<StampExchangeResponse>>> getRecentExchanges(
            @RequestParam Long storeId) {
        List<StampExchangeResponse> responses =
                customerAnalysisService.findAllExchangesForStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, responses));
    }
}
