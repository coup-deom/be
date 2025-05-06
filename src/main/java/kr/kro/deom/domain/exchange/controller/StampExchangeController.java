package kr.kro.deom.domain.exchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
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
    public ResponseEntity<List<StampExchangeResponse>> getMyStoreExchanges() {
        return ResponseEntity.ok(stampExchangeService.getMyStoreExchanges());
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
}
