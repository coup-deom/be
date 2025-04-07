package kr.kro.deom.domain.store.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.deom.entity.Deom;
import kr.kro.deom.domain.deom.repository.DeomRepository;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.store.dto.response.DeomStatus;
import kr.kro.deom.domain.store.dto.response.StoreResponse;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.repository.StoreRepository;
import kr.kro.deom.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final StoreRepository storeRepository;
    private final DeomRepository deomRepository;
    private final MyStampRepository myStampRepository;
    private final UserRepository userRepository;

    public List<StoreResponse> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        Long userId = SecurityUtils.getCurrentUserId();
        return stores.stream()
                .map(store -> toStoreResponse(store, userId))
                .collect(Collectors.toList());
    }

    public List<StoreResponse> getStampedStores() {
        List<Store> stores = storeRepository.findAll();
        Long userId = SecurityUtils.getCurrentUserId();
        return stores.stream()
                .filter(store -> getMyStampCount(store.getId(), userId) > 0)
                .map(store -> toStoreResponse(store, userId))
                .collect(Collectors.toList());
    }

    public List<StoreResponse> getAvailableStores() {
        List<Store> stores = storeRepository.findAll();
        Long userId = SecurityUtils.getCurrentUserId();
        return stores.stream()
                .filter(store -> getMyStampCount(store.getId(), userId) > 0)
                .map(store -> toStoreResponse(store, userId))
                .filter(
                        storeResponse ->
                                storeResponse.getDeoms().stream()
                                        .anyMatch(
                                                deomInfo ->
                                                        deomInfo.getStatus()
                                                                == DeomStatus.AVAILABLE))
                .collect(Collectors.toList());
    }

    private int getMyStampCount(Long storeId, Long userId) {
        Integer stampAmount = myStampRepository.findStampAmountByUserIdAndStoreId(userId, storeId);
        return stampAmount != null ? stampAmount : 0;
    }

    private List<StoreResponse.DeomInfo> getDeomInfoList(Long storeId, Long userId) {
        List<Deom> deoms = deomRepository.findByStoreIdOrderByRequiredStampAmountAsc(storeId);
        List<StoreResponse.DeomInfo> result = new ArrayList<>();

        boolean foundInProgress = false;
        int myStampCount = getMyStampCount(storeId, userId);

        for (Deom deom : deoms) {
            DeomStatus status;

            if (deom.getRequiredStampAmount() <= myStampCount) {
                status = DeomStatus.AVAILABLE;
            } else if (!foundInProgress && myStampCount > 0) {
                status = DeomStatus.IN_PROGRESS;
                foundInProgress = true;
            } else {
                status = DeomStatus.UNAVAILABLE;
            }

            result.add(
                    StoreResponse.DeomInfo.builder()
                            .deomId(deom.getId())
                            .name(deom.getName())
                            .requiredStampAmount(deom.getRequiredStampAmount())
                            .status(status)
                            .build());
        }

        return result;
    }

    private StoreResponse toStoreResponse(Store store, Long userId) {
        Long storeId = store.getId();
        int stampCount = getMyStampCount(storeId, userId);
        return StoreResponse.builder()
                .storeId(storeId)
                .storeName(store.getStoreName())
                .branchName(store.getBranchName())
                .image(store.getImage())
                .city(store.getAddressCity())
                .street(store.getAddressStreet())
                .detail(store.getAddressDetail())
                .myStampCount(stampCount)
                .deoms(getDeomInfoList(storeId, userId))
                .build();
    }
}
