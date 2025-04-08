package kr.kro.deom.domain.otp.controller;

import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.domain.otp.dto.OtpStampApproveRequest;
import kr.kro.deom.domain.otp.service.OtpOwnerStampService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OtpOwnerController {

    private final OtpOwnerStampService otpOwnerStampService;

    @PostMapping("/stamp-request/{optId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveStampOtpRequest(
            @PathVariable Long optId, @RequestBody OtpStampApproveRequest request) {
        return otpOwnerStampService.approveOtpAndAddStamp(optId, request.getAmount());
    }

    @PostMapping("/stamp-request/{optId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectStampOthRequest(@PathVariable Long optId) {
        return otpOwnerStampService.rejectStampOtp(optId);
    }
}
