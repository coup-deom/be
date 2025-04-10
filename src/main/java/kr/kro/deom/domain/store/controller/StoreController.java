package kr.kro.deom.domain.store.controller;

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
    public ResponseEntity<ApiResponse<StoreRegisterResponse>> registerStore(
            @RequestBody @Valid StoreRegisterRequest request) {
        StoreRegisterResponse response = storeService.registerStore(request);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.CREATED, response));
    }
}
