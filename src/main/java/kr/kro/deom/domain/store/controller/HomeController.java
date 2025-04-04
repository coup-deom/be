package kr.kro.deom.domain.store.controller;


import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.domain.store.dto.response.StoreResponse;
import kr.kro.deom.domain.store.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /*
    //전체조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getStores() {
        List<StoreResponse> storeList = homeService.getAllStores();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, storeList));
    }

     */

    @GetMapping("/stamped")
    public String getStampedStores() {
        return "도장 찍은 매장 리스트";
    }

    @GetMapping("/available-coupons")  //지금받을수있는
    public String getAvailableCoupons() {
        return "사용 가능한 쿠폰 리스트";
    }
}