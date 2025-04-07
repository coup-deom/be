//package kr.kro.deom.domain.otp.service;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//import java.time.Instant;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//import kr.kro.deom.common.exception.code.CommonErrorCode;
//import kr.kro.deom.common.utils.SecurityUtils;
//import kr.kro.deom.domain.otp.dto.OtpVerifyResponse;
//import kr.kro.deom.domain.otp.entity.OtpStatus;
//import kr.kro.deom.domain.otp.entity.OtpType;
//import kr.kro.deom.domain.otp.entity.OtpUsage;
//import kr.kro.deom.domain.otp.exception.OtpException;
//import kr.kro.deom.domain.store.repository.StoreRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.redis.core.HashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//
//@ExtendWith(MockitoExtension.class)
//class OtpOwnerServiceTest {
//
//    @Mock private RedisTemplate<String, String> redisTemplate;
//
//    @Mock private HashOperations<String, Object, Object> hashOperations;
//
//    @Mock private OtpUsageRepository otpUsageRepository;
//
//    @Mock private StoreRepository storeRepository;
//
//    @InjectMocks private OtpOwnerService otpOwnerService;
//
//    private final Long testOtpCode = 123456L;
//    private final Long testUserId = 100L;
//    private final Long testStoreId = 200L;
//    private final Long testOtpUsageId = 300L;
//
//    @BeforeEach
//    void setup() {
//        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
//    }
//
//    @Test
//    @DisplayName("Redis에서 OTP 정보를 찾고, DB에 저장되지 않은 경우 성공적으로 검증")
//    void verifyOtpFromRedisSuccessfully() {
//        // Given
//        Map<Object, Object> redisData = createValidRedisData();
//
//        // Redis에 OTP 데이터가 있지만 DB에는 없음
//        when(hashOperations.entries(anyString())).thenReturn(redisData);
//        when(otpUsageRepository.findIdByOtp(testOtpCode)).thenReturn(Optional.empty());
//        when(storeRepository.findOwnerIdByStoreId(testStoreId)).thenReturn(testUserId);
//
//        OtpUsage savedOtpUsage =
//                OtpUsage.builder()
//                        .id(testOtpUsageId)
//                        .otp(testOtpCode)
//                        .userId(testUserId)
//                        .storeId(testStoreId)
//                        .type(OtpType.STAMP)
//                        .status(OtpStatus.PENDING)
//                        .build();
//
//        when(otpUsageRepository.save(any(OtpUsage.class))).thenReturn(savedOtpUsage);
//
//        // SecurityUtils 모킹
//        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
//            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
//
//            // When
//            OtpVerifyResponse response = otpOwnerService.otpVerify(testOtpCode);
//
//            // Then
//            assertNotNull(response);
//            assertEquals(testOtpUsageId, response.getOtpId());
//            assertEquals(OtpType.STAMP, response.getType());
//            assertEquals(OtpStatus.PENDING, response.getStatus());
//
//            verify(otpUsageRepository).save(any(OtpUsage.class));
//        }
//    }
//
//    @Test
//    @DisplayName("Redis에서 OTP 정보를 찾고, 이미 DB에 저장된 경우 성공적으로 검증")
//    void verifyExistingOtpFromRedisSuccessfully() {
//        // Given
//        Map<Object, Object> redisData = createValidRedisData();
//        redisData.put("status", "PENDING");
//
//        // Redis와 DB 모두에 OTP 데이터가 있음
//        when(hashOperations.entries(anyString())).thenReturn(redisData);
//        when(otpUsageRepository.findIdByOtp(testOtpCode)).thenReturn(Optional.of(testOtpUsageId));
//        when(storeRepository.findOwnerIdByStoreId(testStoreId)).thenReturn(testUserId);
//
//        // SecurityUtils 모킹
//        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
//            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
//
//            // When
//            OtpVerifyResponse response = otpOwnerService.otpVerify(testOtpCode);
//
//            // Then
//            assertNotNull(response);
//            assertEquals(testOtpUsageId, response.getOtpId());
//            assertEquals(OtpType.STAMP, response.getType());
//            assertEquals(OtpStatus.PENDING, response.getStatus());
//
//            verify(otpUsageRepository, never()).save(any(OtpUsage.class));
//        }
//    }
//
//    @Test
//    @DisplayName("Redis에 정보가 없고 DB에서 OTP 정보를 찾아 성공적으로 검증")
//    void verifyOtpFromDatabaseSuccessfully() {
//        // Given
//        // Redis에 데이터가 없음
//        when(hashOperations.entries(anyString())).thenReturn(new HashMap<>());
//
//        // DB에 OTP 데이터가 있음
//        Map<String, Object> dbData = new HashMap<>();
//        dbData.put("id", testOtpUsageId);
//        dbData.put("type", OtpType.STAMP);
//        dbData.put("status", OtpStatus.PENDING);
//        dbData.put("storeId", testStoreId);
//
//        when(otpUsageRepository.findIdAndTypeAndStatusByOtp(testOtpCode))
//                .thenReturn(Optional.of(dbData));
//        when(storeRepository.findOwnerIdByStoreId(testStoreId)).thenReturn(testUserId);
//
//        // SecurityUtils 모킹
//        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
//            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
//
//            // When
//            OtpVerifyResponse response = otpOwnerService.otpVerify(testOtpCode);
//
//            // Then
//            assertNotNull(response);
//            assertEquals(testOtpUsageId, response.getOtpId());
//            assertEquals(OtpType.STAMP, response.getType());
//            assertEquals(OtpStatus.PENDING, response.getStatus());
//        }
//    }
//
//    @Test
//    @DisplayName("잘못된 OTP 코드로 예외 발생")
//    void invalidOtpThrowsException() {
//        // Given
//        // Redis와 DB 모두에 데이터가 없음
//        when(hashOperations.entries(anyString())).thenReturn(new HashMap<>());
//        when(otpUsageRepository.findIdAndTypeAndStatusByOtp(testOtpCode))
//                .thenReturn(Optional.empty());
//
//        // When & Then
//        OtpException exception =
//                assertThrows(OtpException.class, () -> otpOwnerService.otpVerify(testOtpCode));
//
//        assertEquals(CommonErrorCode.OTP_INVALID, exception.getBaseResponseCode());
//    }
//
//    @Test
//    @DisplayName("만료된 OTP로 예외 발생")
//    void expiredOtpThrowsException() {
//        // Given
//        // Redis에 데이터가 없음
//        when(hashOperations.entries(anyString())).thenReturn(new HashMap<>());
//
//        // DB에 만료된 OTP 데이터가 있음
//        Map<String, Object> dbData = new HashMap<>();
//        dbData.put("id", testOtpUsageId);
//        dbData.put("type", OtpType.STAMP);
//        dbData.put("status", OtpStatus.EXPIRED);
//        dbData.put("storeId", testStoreId);
//
//        when(otpUsageRepository.findIdAndTypeAndStatusByOtp(testOtpCode))
//                .thenReturn(Optional.of(dbData));
//        when(storeRepository.findOwnerIdByStoreId(testStoreId)).thenReturn(testUserId);
//
//        // SecurityUtils 모킹
//        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
//            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
//
//            // When & Then
//            OtpException exception =
//                    assertThrows(OtpException.class, () -> otpOwnerService.otpVerify(testOtpCode));
//
//            assertEquals(CommonErrorCode.OPT_EXPIRED, exception.getBaseResponseCode());
//        }
//    }
//
//    @Test
//    @DisplayName("이미 처리된 OTP로 예외 발생")
//    void alreadyProcessedOtpThrowsException() {
//        // Given
//        // Redis에 데이터가 없음
//        when(hashOperations.entries(anyString())).thenReturn(new HashMap<>());
//
//        // DB에 이미 처리된 OTP 데이터가 있음
//        Map<String, Object> dbData = new HashMap<>();
//        dbData.put("id", testOtpUsageId);
//        dbData.put("type", OtpType.STAMP);
//        dbData.put("status", OtpStatus.APPROVED); // 이미 사용됨
//        dbData.put("storeId", testStoreId);
//
//        when(otpUsageRepository.findIdAndTypeAndStatusByOtp(testOtpCode))
//                .thenReturn(Optional.of(dbData));
//        when(storeRepository.findOwnerIdByStoreId(testStoreId)).thenReturn(testUserId);
//
//        // SecurityUtils 모킹
//        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
//            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
//
//            // When & Then
//            OtpException exception =
//                    assertThrows(OtpException.class, () -> otpOwnerService.otpVerify(testOtpCode));
//
//            assertEquals(CommonErrorCode.OPT_ALREADY_PROCESSED, exception.getBaseResponseCode());
//        }
//    }
//
//    @Test
//    @DisplayName("매장 소유권이 없는 경우 예외 발생")
//    void unauthorizedStoreAccessThrowsException() {
//        // Given
//        Map<Object, Object> redisData = createValidRedisData();
//
//        // Redis에 OTP 데이터가 있음
//        when(hashOperations.entries(anyString())).thenReturn(redisData);
//        when(storeRepository.findOwnerIdByStoreId(testStoreId)).thenReturn(999L); // 다른 사용자 ID
//
//        // SecurityUtils 모킹
//        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
//            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
//
//            // When & Then
//            OtpException exception =
//                    assertThrows(OtpException.class, () -> otpOwnerService.otpVerify(testOtpCode));
//
//            assertEquals(CommonErrorCode.OTP_UNAUTHORIZED, exception.getBaseResponseCode());
//        }
//    }
//
//    @Test
//    @DisplayName("Redis에서 OTP 정보가 잘못된 형식일 경우 예외 발생")
//    void malformedRedisDataThrowsException() {
//        // Given
//        Map<Object, Object> invalidRedisData = new HashMap<>();
//        invalidRedisData.put("type", "INVALID_TYPE"); // 잘못된 타입
//        invalidRedisData.put("storeId", testStoreId.toString());
//        invalidRedisData.put("userId", testUserId.toString());
//
//        when(hashOperations.entries(anyString())).thenReturn(invalidRedisData);
//        // when(storeRepository.findOwnerIdByStoreId(testStoreId)).thenReturn(testUserId);
//
//        // SecurityUtils 모킹
//        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
//            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
//
//            // When & Then
//            OtpException exception =
//                    assertThrows(OtpException.class, () -> otpOwnerService.otpVerify(testOtpCode));
//
//            assertEquals(CommonErrorCode.OTP_INVALID, exception.getBaseResponseCode());
//        }
//    }
//
//    // 테스트용 Redis 데이터 생성 헬퍼 메서드
//    private Map<Object, Object> createValidRedisData() {
//        Map<Object, Object> redisData = new HashMap<>();
//        redisData.put("type", "STAMP");
//        redisData.put("storeId", testStoreId.toString());
//        redisData.put("userId", testUserId.toString());
//        redisData.put("deomId", "1");
//        redisData.put("usedStampAmount", "5");
//        redisData.put("createdAt", Instant.now().toString());
//        return redisData;
//    }
//}
