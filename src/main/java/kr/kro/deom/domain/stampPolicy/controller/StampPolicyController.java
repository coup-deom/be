package kr.kro.deom.domain.stampPolicy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.stampPolicy.dto.*;
import kr.kro.deom.domain.stampPolicy.service.StampPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stamp-policies")
@Tag(name = "Stamp Policy", description = "스탬프 정책 관련 API")
public class StampPolicyController {

    private final StampPolicyService stampPolicyService;

    @GetMapping("/{storeId}")
    @Operation(
            summary = "스탬프 정책 목록 조회",
            description = "해당 매장의 모든 스탬프 정책을 조회합니다.",
            security = @SecurityRequirement(name = "access-token"))
    public ResponseEntity<ApiResponse<List<StampPolicyDto>>> getAll(
            @Parameter(description = "매장 ID", required = true) @PathVariable Long storeId) {
        List<StampPolicyDto> response = stampPolicyService.getStampPolicy(storeId);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @PostMapping
    @Operation(summary = "스탬프 정책 생성", description = "새로운 스탬프 정책을 생성합니다.")
    public ResponseEntity<ApiResponse<StampPolicyResponse>> saveStampPolicy(
            @RequestBody @Valid StampPolicyRequest stampPolicyRequest) {
        StampPolicyResponse response = stampPolicyService.createStampPolicy(stampPolicyRequest);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @PutMapping("/{policyId}")
    @Operation(summary = "스탬프 정책 수정", description = "기존 스탬프 정책을 수정합니다.")
    public ResponseEntity<ApiResponse<StampPolicyResponse>> updateStampPolicy(
            @Parameter(description = "정책 ID", required = true) @PathVariable Long policyId,
            @RequestBody @Valid StampPolicyUpdateRequest stampPolicyRequest) {
        StampPolicyResponse response =
                stampPolicyService.updateStampPolicy(policyId, stampPolicyRequest);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @DeleteMapping("/{storeId}/{policyId}")
    @Operation(summary = "스탬프 정책 삭제", description = "해당 매장의 스탬프 정책을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteStampPolicy(
            @Parameter(description = "정책 ID", required = true) @PathVariable Long policyId,
            @Parameter(description = "매장 ID", required = true) @PathVariable Long storeId) {
        stampPolicyService.deleteStampPolicy(policyId, storeId);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK));
    }
}
