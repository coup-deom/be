package kr.kro.deom.domain.exchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.exchange.dto.StampExchangeResponse;
import kr.kro.deom.domain.exchange.service.StampExchangeService;
import kr.kro.deom.domain.otp.dto.request.OtpStampRequest;
import kr.kro.deom.domain.otp.dto.response.OtpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exchanges")
@Tag(name = "Stamp Exchanges", description = "스탬프 거래소")
public class StampExchangeController {

    private final StampExchangeService stampExchangeService;

    @GetMapping
    @Operation(summary = "전체 거래 진행 목록", description = "전체 거래 진행 목록")
    public ResponseEntity<ApiResponse<List<StampExchangeResponse>>> getAllExchanges(){
       List<StampExchangeResponse> response  = stampExchangeService.getAllExchanges();
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }
}
