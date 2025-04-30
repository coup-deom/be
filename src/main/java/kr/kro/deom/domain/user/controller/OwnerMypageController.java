package kr.kro.deom.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.user.dto.OwnerMyPageResponse;
import kr.kro.deom.domain.user.service.OwnerMyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/owners/me")
@Tag(name = "사장 마이페이지 API", description = "사장님 마이페이지")
public class OwnerMypageController {
    private final OwnerMyPageService ownerMyPageService;

    @GetMapping
    @Operation(summary = "사장 마이페이지 조회", security = @SecurityRequirement(name = "access-token"))
    public ResponseEntity<ApiResponse<OwnerMyPageResponse>> getOwnerMyPage() {
        OwnerMyPageResponse response = ownerMyPageService.getOwnerMyPage();
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }
}
