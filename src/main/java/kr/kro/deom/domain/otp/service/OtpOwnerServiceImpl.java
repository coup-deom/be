package kr.kro.deom.domain.otp.service;

import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpOwnerServiceImpl implements OtpOwnerService {
    private final OtpRepository otpRepository;
    private final OtpRedisService otpRedisService;


    @Override
    public boolean approveOtp(Long otpCode, Long userId, Long storeId) {
        // PostgreSQL
        OtpUsage otpUsage =
                otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);

        if (otpUsage == null || !userId.equals(otpUsage.getUserId())) {
            return false;
        }

        otpUsage.setStatus(OtpStatus.APPROVED);
        otpRepository.save(otpUsage);

        // redis
        otpRedisService.deleteOtpFromRedis(otpCode);

        return true;
    }

    @Override
    public boolean rejectOtp(Long otpCode, Long userId, Long storeId) {
        // PostgreSQL
        OtpUsage otpUsage =
                otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);

        if (otpUsage == null || !userId.equals(otpUsage.getUserId())) {
            return false;
        }

        otpUsage.setStatus(OtpStatus.REJECTED);
        otpRepository.save(otpUsage);

        // redis
        otpRedisService.deleteOtpFromRedis(otpCode);

        return true;
    }



    /** Verify OTP - 사장님이 otp입력 받았을 때 여기 조회해서 otp정보 받으면 됩 */
    @Override
    public OtpRedisDto verifyOtp(Long otpCode, Long userId, Long storeId) {
        // 처음에는 레디스에서 조회
        OtpRedisDto otpRedisDto = otpRedisService.getOtpFromRedis(otpCode);

        if (otpRedisDto != null) {
            if (storeId.equals(otpRedisDto.getStoreId())) {
                return otpRedisDto;
            }
            return null;
        }

        // 레디스 장애로 조회 실패한 경우 postgreSQL에서 조회 후 변환 후 반환
        OtpUsage otpUsage =
                otpRepository.findByOtpAndStoreIdAndStatus(otpCode, storeId, OtpStatus.PENDING);

        if (otpUsage == null || !userId.equals(otpUsage.getUserId())) {
            return null;
        }

        return OtpRedisDto.convertToOtpRedisDto(otpUsage);
    }




//
//    private static final String REDIS_OTP_PREFIX = "otp:";
//
//    private final RedisTemplate<String, String> redisTemplate;
//    private final OtpUsageRepository otpUsageRepository;
//    private final StoreRepository storeRepository;
//
//    /** OTP 코드 검증 - 메서드 */
//    @Transactional
//    public OtpVerifyResponse otpVerify(Long otpCode) {
//
//        // Redis에서 OTP 정보 조회 시도
//        OtpVerifyResponse redisResult = validateOtpFromRedis(otpCode);
//        if (redisResult != null) {
//            return redisResult;
//        }
//
//        // Redis에 없는 경우 DB에서 조회
//        return validateOtpFromDatabase(otpCode);
//    }
//
//    /** Redis에서 OTP 정보 조회 및 검증 */
//    private OtpVerifyResponse validateOtpFromRedis(Long otpCode) {
//        String redisKey = REDIS_OTP_PREFIX + otpCode;
//        Map<Object, Object> otpInfo = redisTemplate.opsForHash().entries(redisKey);
//
//        // Redis에 정보가 없거나 필수 필드가 없는 경우
//        if (otpInfo == null
//                || otpInfo.isEmpty()
//                || !otpInfo.containsKey("type")
//                || !otpInfo.containsKey("storeId")
//                || !otpInfo.containsKey("userId")) {
//            return null;
//        }
//
//        try {
//            // Redis 데이터 파싱
//            String typeStr = (String) otpInfo.get("type");
//            OtpType type = OtpType.valueOf(typeStr.toUpperCase());
//            Long storeId = Long.valueOf(otpInfo.get("storeId").toString());
//
//            // 매장 소유자 검증
//            validateStoreOwnership(storeId);
//
//            // DB에서 OTP ID 조회
//            Optional<Long> optionalId = otpUsageRepository.findIdByOtp(otpCode);
//
//            if (optionalId.isPresent()) {
//                // 이미 DB에 존재하는 경우
//                Long otpUsageId = optionalId.get();
//
//                // Redis에서 상태 정보 확인 (status 필드가 있다고 가정)
//                if (otpInfo.containsKey("status")) {
//                    String statusStr = otpInfo.get("status").toString();
//                    if (!statusStr.equals("PENDING")) {
//                        throw new OtpException(CommonErrorCode.OPT_ALREADY_PROCESSED);
//                    }
//                }
//
//                return new OtpVerifyResponse(otpUsageId, type, OtpStatus.PENDING);
//            } else {
//                // Redis에는 있지만 DB에 없는 경우 -> DB에 저장
//                OtpUsage newOtpUsage = createOtpUsageFromRedis(otpCode, otpInfo);
//                OtpUsage savedOtpUsage = otpUsageRepository.save(newOtpUsage);
//                return new OtpVerifyResponse(savedOtpUsage.getId(), type, OtpStatus.PENDING);
//            }
//        } catch (OtpException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new OtpException(CommonErrorCode.OTP_INVALID);
//        }
//    }
//
//    /** DB에서 OTP 정보 조회 및 검증 */
//    private OtpVerifyResponse validateOtpFromDatabase(Long otpCode) {
//        Optional<Map<String, Object>> optionalOtpInfo =
//                otpUsageRepository.findIdAndTypeAndStatusByOtp(otpCode);
//
//        // DB에 없는 경우 -> 완전 잘못된 OTP
//        if (optionalOtpInfo.isEmpty()) {
//            throw new OtpException(CommonErrorCode.OTP_INVALID);
//        }
//
//        // DB에 있는 경우 정보 추출
//        Map<String, Object> dbOtpInfo = optionalOtpInfo.get();
//        Long storeId = (Long) dbOtpInfo.get("storeId");
//
//        // 매장 소유자 검증
//        validateStoreOwnership(storeId);
//
//        Long id = (Long) dbOtpInfo.get("id");
//        OtpType type = (OtpType) dbOtpInfo.get("type");
//        OtpStatus status = (OtpStatus) dbOtpInfo.get("status");
//
//        // OTP 상태 검증
//        checkOtpStatus(status);
//
//        return new OtpVerifyResponse(id, type, status);
//    }
//
//    /** Redis 데이터로 OtpUsage 엔티티 생성 */
//    private OtpUsage createOtpUsageFromRedis(Long otpCode, Map<Object, Object> otpInfo) {
//        // deomId와 usedStampAmount 파싱
//        Integer deomId = null;
//        if (otpInfo.get("deomId") != null && !otpInfo.get("deomId").equals("null")) {
//            deomId = Integer.valueOf(otpInfo.get("deomId").toString());
//        }
//
//        Integer usedStampAmount = null;
//        if (otpInfo.get("usedStampAmount") != null) {
//            usedStampAmount = Integer.valueOf(otpInfo.get("usedStampAmount").toString());
//        }
//
//        // 빌더로 OtpUsage 객체 생성
//        return OtpUsage.builder()
//                .otp(otpCode)
//                .userId(Long.valueOf(otpInfo.get("userId").toString()))
//                .storeId(Long.valueOf(otpInfo.get("storeId").toString()))
//                .type(OtpType.valueOf(otpInfo.get("type").toString().toUpperCase()))
//                .deomId(deomId)
//                .usedStampAmount(usedStampAmount)
//                .createdAt(Instant.parse(otpInfo.get("createdAt").toString()))
//                .status(OtpStatus.PENDING)
//                .build();
//    }
//
//    /** OTP 상태 검증 */
//    private void checkOtpStatus(OtpStatus status) {
//        if (status == OtpStatus.EXPIRED) {
//            throw new OtpException(CommonErrorCode.OPT_EXPIRED);
//        } else if (status != OtpStatus.PENDING) {
//            throw new OtpException(CommonErrorCode.OPT_ALREADY_PROCESSED);
//        }
//    }
//
//    /** 매장 소유자 검증 */
//    private void validateStoreOwnership(Long storeId) {
//        Long userId = SecurityUtils.getCurrentUserId();
//        Long ownerId = storeRepository.findOwnerIdByStoreId(storeId);
//
//        if (userId == null || ownerId == null || !userId.equals(ownerId)) {
//            throw new OtpException(CommonErrorCode.OTP_UNAUTHORIZED);
//        }
//    }
}
