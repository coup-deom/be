package kr.kro.deom.domain.deom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.deom.dto.DeomDto;
import kr.kro.deom.domain.deom.dto.DeomRequest;
import kr.kro.deom.domain.deom.dto.DeomResponse;
import kr.kro.deom.domain.deom.dto.DeomUpdateRequest;
import kr.kro.deom.domain.deom.service.DeomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/deom-policies")
@Tag(name = "Deom Policy", description = "사장님 마이페이지 - 덤 정책 관련 API")
public class DeomController {

    private final DeomService deomService;

    @GetMapping("/{storeId}")
    @Operation(
            summary = "덤 정책 목록 조회",
            description = "해당 매장의 모든 덤 정책을 조회합니다.",
            security = @SecurityRequirement(name = "access-token"))
    public ResponseEntity<ApiResponse<List<DeomDto>>> getAll(
            @Parameter(description = "매장 ID", required = true) @PathVariable Long storeId) {
        List<DeomDto> response = deomService.getDeomPolicy(storeId);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @PostMapping
    @Operation(summary = "덤 정책 생성", description = "새로운 덤 정책을 생성합니다.")
    public ResponseEntity<ApiResponse<DeomResponse>> saveDeomPolicy(
            @RequestBody @Valid DeomRequest deomRequest) {
        DeomResponse response = deomService.createDeomPolicy(deomRequest);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @PutMapping("/{deomId}")
    @Operation(summary = "덤 정책 수정", description = "기존 덤 정책을 수정합니다.")
    public ResponseEntity<ApiResponse<DeomResponse>> updateDeomPolicy(
            @Parameter(description = "덤 ID", required = true) @PathVariable Long deomId,
            @RequestBody @Valid DeomUpdateRequest deomRequest) {
        DeomResponse response = deomService.updateDeomPolicy(deomId, deomRequest);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @DeleteMapping("/{storeId}/{deomId}")
    @Operation(summary = "덤 정책 삭제", description = "해당 매장의 덤 정책을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteStampPolicy(
            @Parameter(description = "덤 ID", required = true) @PathVariable Long deomId,
            @Parameter(description = "매장 ID", required = true) @PathVariable Long storeId) {
        deomService.deleteDeomPolicy(deomId, storeId);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK));
    }
}
