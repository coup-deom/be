package kr.kro.deom.domain.store.controller;

import java.util.List;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.domain.store.dto.response.StoreResponse;
import kr.kro.deom.domain.store.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class HomeController {

  private final HomeService homeService;

  // 전체조회
  @GetMapping
  public ResponseEntity<ApiResponse<List<StoreResponse>>> getStores() {
    List<StoreResponse> storeList = homeService.getAllStores();
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, storeList));
  }

  // 스탬프 존재하는 것만
  @GetMapping("/stamped")
  public ResponseEntity<ApiResponse<List<StoreResponse>>> getStampedStores() {
    List<StoreResponse> storeList = homeService.getStampedStores();
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, storeList));
  }

  // 받을 수 있는 덤이 있는 것만.
  @GetMapping("/available-stores") // 지금받을수있는
  public ResponseEntity<ApiResponse<List<StoreResponse>>> getAvailableStores() {
    List<StoreResponse> storeList = homeService.getAvailableStores();
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, storeList));
  }
}
