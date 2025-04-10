package kr.kro.deom.domain.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.domain.store.dto.request.StoreRegisterRequest;
import kr.kro.deom.domain.store.dto.response.StoreRegisterResponse;
import kr.kro.deom.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    @Operation(summary = "사장 가게 등록 신청", description = "사장이 가게 등록을 신청한다.")
    public ResponseEntity<ApiResponse<StoreRegisterResponse>> registerStore(
            @RequestBody @Valid StoreRegisterRequest request) {
        StoreRegisterResponse response = storeService.registerStore(request);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.CREATED, response));
    }
}
