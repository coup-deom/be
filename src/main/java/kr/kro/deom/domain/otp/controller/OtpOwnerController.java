package kr.kro.deom.domain.otp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.domain.otp.dto.request.DeomUsageRequestDto;
import kr.kro.deom.domain.otp.dto.request.OtpStampApproveRequest;
import kr.kro.deom.domain.otp.dto.response.OwnerStampInfoResponse;
import kr.kro.deom.domain.otp.service.OtpOwnerDeomService;
import kr.kro.deom.domain.otp.service.OtpOwnerStampService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "OTP 요청 처리", description = "사장님이 보는 OTP 스탬프 승인/거절/조회 API")
public class OtpOwnerController {

    private final OtpOwnerStampService otpOwnerStampService;
    private final OtpOwnerDeomService otpOwnerDeomService;

    @Operation(summary = "스탬프 요청 상세 조회", description = "고객의 적립 현황과 가게의 스탬프 정책을 조회합니다.")
    @GetMapping("/stamp-requests/{storeId}/{otpCode}")
    public ResponseEntity<ApiResponse<OwnerStampInfoResponse>> getStampGuide(
            @PathVariable Long storeId, @PathVariable Long otpCode) {
        return otpOwnerStampService.getUserStampStatusAndStampPolicy(otpCode, storeId);
    }

    @Operation(summary = "스탬프 요청 승인", description = "OTP 요청을 승인하고 고객에게 스탬프를 적립합니다.")
    @PostMapping("/stamp-request/approval")
    public ResponseEntity<ApiResponse<Void>> approveStampOtpRequest(
            @RequestBody OtpStampApproveRequest request) {
        return otpOwnerStampService.approveOtpAndAddStamp(
                request.getOtpCode(), request.getStoreId(), request.getAmount());
    }

    @Operation(summary = "스탬프 요청 거절", description = "OTP 요청을 거절하고 삭제합니다.")
    @PostMapping("/stamp-request/rejection")
    public ResponseEntity<ApiResponse<Void>> rejectStampOthRequest(
            @RequestBody OtpStampApproveRequest request) {
        return otpOwnerStampService.rejectStampOtp(request.getOtpCode(), request.getStoreId());
    }

    //    @Operation(summary = "덤 요청 상세 조회", description = "고객의 적립 현황과 가게의 덤 정책을 조회합니다.")
    //    @GetMapping("/deom-requests/{storeId}/{otpCode}")
    //    public ResponseEntity<ApiResponse<OwnerDeomInfoResponse>> getStampGuide(
    //            @PathVariable Long storeId, @PathVariable Long otpCode) {
    //        return otpOwnerDeomService.getUserStampStatusAndDeomPolicy(otpCode, storeId);
    //    }

    @Operation(summary = "덤 요청 승인", description = "OTP 요청을 승인하고 고객 스탬프를 소진합니다.")
    @PostMapping("/deom-requests/approval")
    public ResponseEntity<ApiResponse<Void>> approveDeomOtpRequest(
            @RequestBody DeomUsageRequestDto deomUsageRequestDto) {
        return otpOwnerDeomService.approveOtp(deomUsageRequestDto);
    }

    @Operation(summary = "덤 요청 거절", description = "OTP 요청을 거절하고 삭제합니다.")
    @PostMapping("/deom-requests/rejection")
    public ResponseEntity<ApiResponse<Void>> rejectDeomOthRequest(
            @RequestBody DeomUsageRequestDto deomUsageRequestDto) {
        return otpOwnerDeomService.rejectOtp(deomUsageRequestDto);
    }
}
