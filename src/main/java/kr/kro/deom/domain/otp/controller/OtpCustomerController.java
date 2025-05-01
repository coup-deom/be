package kr.kro.deom.domain.otp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.otp.dto.request.OtpDeomRequest;
import kr.kro.deom.domain.otp.dto.request.OtpStampRequest;
import kr.kro.deom.domain.otp.dto.response.OtpResponse;
import kr.kro.deom.domain.otp.service.OtpCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
@Tag(name = "OTP 발급 API")
public class OtpCustomerController {

    private final OtpCustomerService otpCustomerService;

    @PostMapping("/request/stamp")
    @Operation(summary = "스탬프 적립 OTP 발급", description = "스탬프 적립 요청 시 OTP를 발급한다.")
    public ResponseEntity<ApiResponse<OtpResponse>> issueStampOtp(
            @RequestBody @Valid OtpStampRequest request) {
        System.out.println(request.getType());
        OtpResponse response = otpCustomerService.issueStampOtp(request);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @PostMapping("/request/deom")
    @Operation(summary = "스탬프 소진 OTP 발급", description = "스탬프 소진 요청 시 OTP를 발급한다.")
    public ResponseEntity<ApiResponse<OtpResponse>> issueDeomOtp(
            @RequestBody @Valid OtpDeomRequest request) {
        OtpResponse response = otpCustomerService.issueDeomOtp(request);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }
}
