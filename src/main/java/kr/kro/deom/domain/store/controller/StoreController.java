package kr.kro.deom.domain.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.store.dto.request.StoreRegisterRequest;
import kr.kro.deom.domain.store.dto.response.StoreRegisterResponse;
import kr.kro.deom.domain.store.dto.response.StoreSelectResponse;
import kr.kro.deom.domain.store.dto.response.StoreStatusRequest;
import kr.kro.deom.domain.store.dto.response.StoreStatusResponse;
import kr.kro.deom.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Tag(name = "Store", description = "가게 API")
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    @Operation(summary = "사장 가게 등록 신청", description = "사장이 가게 등록을 신청한다.")
    public ResponseEntity<ApiResponse<StoreRegisterResponse>> registerStore(
            @RequestBody @Valid StoreRegisterRequest request) {
        StoreRegisterResponse response = storeService.registerStore(request);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @GetMapping("/all")
    @Operation(summary = "모든 가게 목록 조회", description = "등록된 전체 가게 목록을 조회합니다(거래소 가게 선택에 사용)")
    public ResponseEntity<ApiResponse<List<StoreSelectResponse>>> getAllStores() {
        List<StoreSelectResponse> response = storeService.getAllStores();
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @GetMapping("/my-stamps")
    @Operation(summary = "스탬프 보유 목록 조회", description = "사용자가 스탬프를 보유한 가게를 조회합니다(거래소 가게 선택에 사용)")
    public ResponseEntity<ApiResponse<List<StoreSelectResponse>>> getMyStampStores() {
        List<StoreSelectResponse> response = storeService.getMyStampStores();
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @GetMapping("/{storeId}/status")
    @Operation(summary = "가게 입점 상태", description = "가게의 입점 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<StoreStatusResponse>> getStoreStatus(@RequestBody StoreStatusRequest request) {
        StoreStatusResponse response = storeService.getStoreStatus(request.getStoreId());
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }


}
