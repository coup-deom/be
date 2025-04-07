package kr.kro.deom.domain.customer.controller;

import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.customer.dto.OtpDeomRequest;
import kr.kro.deom.domain.customer.dto.OtpResponse;
import kr.kro.deom.domain.customer.dto.OtpStampRequest;
import kr.kro.deom.domain.customer.service.CustomerOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class CustomerOtpController {

  private final CustomerOtpService customerOtpService;

  @PostMapping("/request/stamp")
  public ResponseEntity<ApiResponse<OtpResponse>> issueStampOtp(
      @RequestBody OtpStampRequest request) {
    OtpResponse response = customerOtpService.issueStampOtp(request);
    return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
  }

  @PostMapping("/request/deom")
  public ResponseEntity<ApiResponse<OtpResponse>> issueStampOtp(
      @RequestBody OtpDeomRequest request) {
    OtpResponse response = customerOtpService.issueDeomOtp(request);
    return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
  }
}
