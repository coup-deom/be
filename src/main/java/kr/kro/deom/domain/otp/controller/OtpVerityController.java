package kr.kro.deom.domain.otp.controller;

import jakarta.validation.Valid;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.otp.dto.request.OtpVerifyRequest;
import kr.kro.deom.domain.otp.dto.response.OtpVerifyResponse;
import kr.kro.deom.domain.otp.service.OtpOwnerServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OtpVerityController {

    private final OtpOwnerServiceImpl otpOwnerService;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<OtpVerifyResponse>> verifyOtp(
            @RequestBody @Valid OtpVerifyRequest request) {
        OtpVerifyResponse response = otpOwnerService.otpVerify(request.getOtpCode());
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }
}
