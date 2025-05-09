package kr.kro.deom.domain.exchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.exchange.dto.StampExchangeExecutionResponse;
import kr.kro.deom.domain.exchange.dto.StampExchangeRequest;
import kr.kro.deom.domain.exchange.dto.StampExchangeResponse;
import kr.kro.deom.domain.exchange.dto.StampExchangeUpdateRequest;
import kr.kro.deom.domain.exchange.service.StampExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exchanges")
@Tag(name = "Stamp Exchanges", description = "스탬프 거래소")
public class StampExchangeController {

    private final StampExchangeService stampExchangeService;

    @GetMapping
    @Operation(summary = "전체 거래 진행 목록", description = "전체 거래 진행 목록")
    public ResponseEntity<ApiResponse<List<StampExchangeResponse>>> getAllExchanges() {
        List<StampExchangeResponse> response = stampExchangeService.getAllExchanges();
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @GetMapping("/my")
    @Operation(summary = "내가 등록한 가게만 목록", description = "내가 등록한 가게 거래 목록")
    public ResponseEntity<ApiResponse<List<StampExchangeResponse>>> getMyStoreExchanges() {
        List<StampExchangeResponse> response = stampExchangeService.getMyStoreExchanges();
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @GetMapping("/tradable")
    @Operation(summary = "거래 가능한 가게만 목록", description = "내가 거래할 수 있는 가게 목록")
    public ResponseEntity<ApiResponse<List<StampExchangeResponse>>> getTradableExchanges() {
        List<StampExchangeResponse> response = stampExchangeService.getTradableExchanges();
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @PostMapping
    @Operation(summary = "스탬프 거래 생성", description = "유저가 스탬프 거래를 생성합니다.")
    public ResponseEntity<ApiResponse<StampExchangeResponse>> createStampExchange(
            @RequestBody StampExchangeRequest request) {
        StampExchangeResponse response = stampExchangeService.createStampExchange(request);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @PutMapping("/{stampExchangeId}")
    @Operation(summary = "스탬프 거래 수정", description = "유저가 기존 스탬프 거래 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<StampExchangeResponse>> updateStampExchange(
            @PathVariable Long stampExchangeId, @RequestBody StampExchangeUpdateRequest request) {
        StampExchangeResponse response =
                stampExchangeService.updateStampExchange(stampExchangeId, request);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @DeleteMapping("/{stampExchangeId}")
    @Operation(summary = "스탬프 거래 삭제", description = "스탬프 거래를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteStampExchange(
            @PathVariable Long stampExchangeId) {
        stampExchangeService.deleteStampExchange(stampExchangeId);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK));
    }

    @PostMapping("/{exchangeId}/execute")
    @Operation(summary = "스탬프 거래 실행", description = "스탬프가 교환됩니다.")
    public ResponseEntity<ApiResponse<StampExchangeExecutionResponse>> executeExchange(
            @PathVariable Long exchangeId) {
        StampExchangeExecutionResponse response = stampExchangeService.executeExchange(exchangeId);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }
}
