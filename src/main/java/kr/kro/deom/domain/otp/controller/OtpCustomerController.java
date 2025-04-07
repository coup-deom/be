package kr.kro.deom.domain.otp.controller;

import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.otp.dto.OtpDeomRequest;
import kr.kro.deom.domain.otp.dto.OtpResponse;
import kr.kro.deom.domain.otp.dto.OtpStampRequest;
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

public class OtpCustomerController {

    private final OtpCustomerService otpCustomerService;


    @PostMapping("/request/stamp")
    public ResponseEntity<ApiResponse<OtpResponse>> issueStampOtp(
            @RequestBody OtpStampRequest request) {
        OtpResponse response = otpCustomerService.issueStampOtp(request);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @PostMapping("/request/deom")
    public ResponseEntity<ApiResponse<OtpResponse>> issueDeomOtp(
            @RequestBody OtpDeomRequest request) {
        OtpResponse response = otpCustomerService.issueDeomOtp(request);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }
}