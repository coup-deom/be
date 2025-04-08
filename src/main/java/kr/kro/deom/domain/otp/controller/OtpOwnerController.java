package kr.kro.deom.domain.otp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.domain.otp.dto.request.OtpStampApproveRequest;
import kr.kro.deom.domain.otp.dto.response.OwnerStampInfoResponse;
import kr.kro.deom.domain.otp.service.OtpOwnerStampService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "OTP 요청 처리", description = "사장님이 보는 OTP 스탬프 승인/거절/조회 API")
public class OtpOwnerController {

    private final OtpOwnerStampService otpOwnerStampService;

    @Operation(summary = "스탬프 요청 상세 조회", description = "고객의 적립 현황과 가게의 스탬프 정책을 조회합니다.")
    @GetMapping("/stamp-requests/{otpId}")
    public ResponseEntity<ApiResponse<OwnerStampInfoResponse>> getStampGuide(
            @PathVariable Long otpId) {
        return otpOwnerStampService.getUserStampStatusAndStampPolicy(otpId);
    }

    @Operation(summary = "스탬프 요청 승인", description = "OTP 요청을 승인하고 고객에게 스탬프를 적립합니다.")
    @PostMapping("/stamp-request/{optId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveStampOtpRequest(
            @PathVariable Long optId, @RequestBody OtpStampApproveRequest request) {
        return otpOwnerStampService.approveOtpAndAddStamp(optId, request.getAmount());
    }

    @Operation(summary = "스탬프 요청 거절", description = "OTP 요청을 거절하고 삭제합니다.")
    @PostMapping("/stamp-request/{optId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectStampOthRequest(@PathVariable Long optId) {
        return otpOwnerStampService.rejectStampOtp(optId);
    }
}
