package kr.kro.deom.domain.user.controller;

import java.util.List;
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
public class CustomerMyPageController {

    private final CustomerOtpUsageService customerOtpUsageService;

    public static final List<OtpStatus> PENDING = List.of(OtpStatus.PENDING);
    public static final List<OtpStatus> COMPLETED = List.of(OtpStatus.APPROVED, OtpStatus.REJECTED);

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<OtpUsageResponse>>> getAll(
            @AuthenticationPrincipal CustomOAuth2User user) {
        List<OtpUsageResponse> response = customerOtpUsageService.getAllUsages(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<OtpUsageResponse>>> getPending(
            @AuthenticationPrincipal CustomOAuth2User user) {
        List<OtpUsageResponse> response =
                customerOtpUsageService.getUsagesByStatus(user.getUserId(), PENDING);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<List<OtpUsageResponse>>> getCompleted(
            @AuthenticationPrincipal CustomOAuth2User user) {
        List<OtpUsageResponse> response =
                customerOtpUsageService.getUsagesByStatus(user.getUserId(), COMPLETED);

        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }
}
