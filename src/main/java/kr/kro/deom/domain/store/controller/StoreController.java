package kr.kro.deom.domain.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.kro.deom.common.file.service.S3FileService;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.store.dto.request.StoreRegisterRequest;
import kr.kro.deom.domain.store.dto.response.StoreImageResponse;
import kr.kro.deom.domain.store.dto.response.StoreRegisterResponse;
import kr.kro.deom.domain.store.dto.response.StoreSelectResponse;
import kr.kro.deom.domain.store.dto.response.StoreStatusResponse;
import kr.kro.deom.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Tag(name = "Store", description = "가게 API")
public class StoreController {

    private final StoreService storeService;
    private final S3FileService s3FileService;

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

    @GetMapping("/status")
    @Operation(summary = "가게 입점 상태", description = "가게의 입점 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<StoreStatusResponse>> getStoreStatus() {
        StoreStatusResponse response = storeService.getStoreStatus();
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 업로드", description = "가게의 로고 이미지를 등록합니다.")
    public ResponseEntity<ApiResponse<StoreImageResponse>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        StoreImageResponse response = storeService.uploadStoreImage(file);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }
}
