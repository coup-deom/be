package kr.kro.deom.domain.otp.controller;

import jakarta.validation.Valid;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.otp.dto.OtpVerifyRequest;
import kr.kro.deom.domain.otp.dto.OtpVerifyResponse;
import kr.kro.deom.domain.otp.service.OtpVerifyService;
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

  private final OtpVerifyService otpVerifyService;

  @PostMapping("/verify")
  public ResponseEntity<ApiResponse<OtpVerifyResponse>> verifyOtp(
      @RequestBody @Valid OtpVerifyRequest request) {
    OtpVerifyResponse response = otpVerifyService.otpVerify(request.getOtpCode());
    return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
  }
}
