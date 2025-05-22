package kr.kro.deom.domain.otp.service;

import java.util.List;
import kr.kro.deom.domain.deom.entity.Deom;
import kr.kro.deom.domain.deom.service.DeomService;
import kr.kro.deom.domain.otp.dto.response.OtpUsageResponse;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpType;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerOtpUsageService {

    private final OtpRepository otpRepository;
    private final StoreService storeService;
    private final DeomService deomService;

    public List<OtpUsageResponse> getAllUsages(Long userId) {
        return mapToResponse(otpRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    public List<OtpUsageResponse> getUsagesByStatus(Long userId, List<OtpStatus> status) {
        return mapToResponse(
                otpRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(userId, status));
    }

    private List<OtpUsageResponse> mapToResponse(List<OtpUsage> usages) {
        return usages.stream()
                .map(
                        usage -> {
                            Store store = storeService.getStore(usage.getStoreId());

                            OtpUsageResponse.OtpUsageResponseBuilder builder =
                                    OtpUsageResponse.builder()
                                            .otpId(usage.getId())
                                            .storeId(store.getId())
                                            .storeName(store.getStoreName())
                                            .storeImage(store.getImage())
                                            .type(usage.getType())
                                            .usedStampAmount(usage.getUsedStampAmount())
                                            .createdAt(usage.getCreatedAt())
                                            .status(usage.getStatus());

                            if (usage.getStatus() == OtpStatus.PENDING) {
                                builder.otpCode(usage.getOtp());
                            }

                            if (usage.getType() == OtpType.DEOM && usage.getDeomId() != null) {
                                Deom deom = deomService.getDeom(usage.getDeomId());
                                builder.deomId(deom.getId()).deomName(deom.getName());
                            }

                            return builder.build();
                        })
                .toList();
    }
}
