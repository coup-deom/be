package kr.kro.deom.domain.deom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.deom.dto.*;
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

    @PostMapping("/{storeId}")
    @Operation(
            summary = "덤 정책 등록/수정/삭제",
            description = "해당 매장의 덤 정책에 대해 등록/수정/삭제합니다.",
            security = @SecurityRequirement(name = "access-token"))
    public ResponseEntity<ApiResponse<DeomsResponse>> updateDeomPolicies(
            @PathVariable Long storeId, @RequestBody DeomsRequest deomsRequest) {
        DeomsResponse response = deomService.updateAllDeomPolicies(storeId, deomsRequest);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }
}
