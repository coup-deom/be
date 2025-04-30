package kr.kro.deom.domain.user.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.common.security.oauth.CustomOAuth2User;
import kr.kro.deom.domain.otp.dto.response.OtpUsageResponse;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.service.CustomerOtpUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customers/request")
@Tag(name = "고객 마이페이지 API")
public class CustomerMyPageController {

    private final CustomerOtpUsageService customerOtpUsageService;

    public static final List<OtpStatus> PENDING = List.of(OtpStatus.PENDING);
    public static final List<OtpStatus> COMPLETED = List.of(OtpStatus.APPROVED, OtpStatus.REJECTED);

    @GetMapping("/all")
    @Operation(summary = "모든 요청 내역 조회", description = "유저가 요청한 내역을 모두 조회한다.")
    public ResponseEntity<ApiResponse<List<OtpUsageResponse>>> getAll(
            @AuthenticationPrincipal CustomOAuth2User user) {
        List<OtpUsageResponse> response = customerOtpUsageService.getAllUsages(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @GetMapping("/pending")
    @Operation(summary = "진행 중인 요청 조회", description = "진행 중인(PENDING) 요청 내역을 조회한다.")
    public ResponseEntity<ApiResponse<List<OtpUsageResponse>>> getPending(
            @AuthenticationPrincipal CustomOAuth2User user) {
        List<OtpUsageResponse> response =
                customerOtpUsageService.getUsagesByStatus(user.getUserId(), PENDING);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @GetMapping("/completed")
    @Operation(summary = "완료된 요청 조회", description = "완료된(APPROVED, REJECTED) 요청 내역을 조회한다.")
    public ResponseEntity<ApiResponse<List<OtpUsageResponse>>> getCompleted(
            @AuthenticationPrincipal CustomOAuth2User user) {
        List<OtpUsageResponse> response =
                customerOtpUsageService.getUsagesByStatus(user.getUserId(), COMPLETED);

        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }
}
