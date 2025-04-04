//package kr.kro.deom.domain.store.controller;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import kr.kro.deom.common.utils.SecurityUtils;
//import kr.kro.deom.domain.store.dto.response.StoreResponse;
//import kr.kro.deom.domain.store.service.HomeService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//@WebMvcTest(controllers = HomeController.class)
//@AutoConfigureMockMvc(addFilters = false)
//public class HomeControllerTest {
//
//  @Configuration
//  static class TestConfig {
//    @Bean
//    public HomeService homeService() {
//      return mock(HomeService.class);
//    }
//  }
//
//  @Autowired private MockMvc mockMvc;
//
//  @Autowired private HomeService homeService;
//
//  private List<StoreResponse> storeResponses;
//
//  @BeforeEach
//  void setUp() {
//    // 모의 객체 초기화
//    reset(homeService);
//
//    // 테스트용 더미 데이터 생성
//    storeResponses =
//        Arrays.asList(
//            StoreResponse.builder()
//                .storeId(1L)
//                .storeName("스토어1")
//                .branchName("브랜치1")
//                .image("image1.jpg")
//                .city("서울")
//                .street("강남대로")
//                .detail("123-45")
//                .myStampCount(5)
//                .deoms(new ArrayList<>())
//                .build(),
//            StoreResponse.builder()
//                .storeId(2L)
//                .storeName("스토어2")
//                .branchName("브랜치2")
//                .image("image2.jpg")
//                .city("부산")
//                .street("해운대로")
//                .detail("67-89")
//                .myStampCount(3)
//                .deoms(new ArrayList<>())
//                .build());
//  }
//
//  @Test
//  @DisplayName("모든 스토어 조회 테스트")
//  void getStoresTest() throws Exception {
//    // given
//    when(homeService.getAllStores()).thenReturn(storeResponses);
//
//    try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
//      securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
//
//      // when & then
//      mockMvc
//          .perform(get("/stores").contentType(MediaType.APPLICATION_JSON))
//          .andDo(print()) // 요청/응답 세부 정보 출력
//          .andExpect(status().isOk())
//          .andExpect(jsonPath("$.code").value(200))
//          .andExpect(jsonPath("$.status").value("OK"))
//          .andExpect(jsonPath("$.message").value("Success"))
//          .andExpect(jsonPath("$.data").isArray())
//          .andExpect(jsonPath("$.data.length()").value(2))
//          .andExpect(jsonPath("$.data[0].storeId").value(1))
//          .andExpect(jsonPath("$.data[0].storeName").value("스토어1"))
//          .andExpect(jsonPath("$.data[1].storeId").value(2))
//          .andExpect(jsonPath("$.data[1].storeName").value("스토어2"));
//    }
//
//    // verify
//    verify(homeService, times(1)).getAllStores();
//  }
//
//  @Test
//  @DisplayName("스탬프가 있는 스토어 조회 테스트")
//  void getStampedStoresTest() throws Exception {
//    // given
//    when(homeService.getStampedStores()).thenReturn(storeResponses);
//
//    try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
//      securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
//
//      // when & then
//      mockMvc
//          .perform(get("/stores/stamped").contentType(MediaType.APPLICATION_JSON))
//          .andDo(print()) // 요청/응답 세부 정보 출력
//          .andExpect(status().isOk())
//          .andExpect(jsonPath("$.code").value(200))
//          .andExpect(jsonPath("$.status").value("OK"))
//          .andExpect(jsonPath("$.message").value("Success"))
//          .andExpect(jsonPath("$.data").isArray())
//          .andExpect(jsonPath("$.data.length()").value(2))
//          .andExpect(jsonPath("$.data[0].storeId").value(1))
//          .andExpect(jsonPath("$.data[0].myStampCount").value(5))
//          .andExpect(jsonPath("$.data[1].storeId").value(2))
//          .andExpect(jsonPath("$.data[1].myStampCount").value(3));
//    }
//
//    // verify
//    verify(homeService, times(1)).getStampedStores();
//  }
//
//  @Test
//  @DisplayName("사용 가능한 덤이 있는 스토어 조회 테스트")
//  void getAvailableStoresTest() throws Exception {
//    // given
//    when(homeService.getAvailableStores()).thenReturn(storeResponses);
//
//    try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
//      securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
//
//      // when & then
//      mockMvc
//          .perform(get("/stores/available-stores").contentType(MediaType.APPLICATION_JSON))
//          .andDo(print()) // 요청/응답 세부 정보 출력
//          .andExpect(status().isOk())
//          .andExpect(jsonPath("$.code").value(200))
//          .andExpect(jsonPath("$.status").value("OK"))
//          .andExpect(jsonPath("$.message").value("Success"))
//          .andExpect(jsonPath("$.data").isArray())
//          .andExpect(jsonPath("$.data.length()").value(2))
//          .andExpect(jsonPath("$.data[0].storeId").value(1))
//          .andExpect(jsonPath("$.data[1].storeId").value(2));
//    }
//
//    // verify
//    verify(homeService, times(1)).getAvailableStores();
//  }
//}
