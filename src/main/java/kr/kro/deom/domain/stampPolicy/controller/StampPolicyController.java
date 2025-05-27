package kr.kro.deom.domain.stampPolicy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Stamp Policy", description = "사장님 마이페이지 - 스탬프 정책 관련 API")
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

    @PutMapping("/{storeId}")
    @Operation(
            summary = "스탬프 정책 등록/수정/삭제",
            description = "해당 매장의 스탬프 정책에 대해 등록/수정/삭제합니다.",
            security = @SecurityRequirement(name = "access-token"))
    public ResponseEntity<ApiResponse<StampPoliciesResponse>> updateStampPolicies(
            @PathVariable Long storeId, @RequestBody StampPoliciesRequest stampPolicies) {
        StampPoliciesResponse response =
                stampPolicyService.updateAllStampPolicies(storeId, stampPolicies);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }
}
