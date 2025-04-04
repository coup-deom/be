package kr.kro.deom.domain.store.service;

import kr.kro.deom.domain.deom.repository.DeomRepository;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.store.dto.response.StoreResponse;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final StoreRepository storeRepository;
    private final DeomRepository deomRepository;
    private final MyStampRepository myStampRepository;
/*
    public List<StoreResponse> getAllStores() {
        List<Store> stores = storeRepository.findAll();

        return stores.stream()
                .map(store -> StoreResponse.builder()
                        .storeId(store.getId())
                        .storeName(store.getName())
                        .branchName(store.getBranch())
                        .address(StoreResponse.Address.builder()
                                .city(store.getCity())
                                .street(store.getStreet())
                                .detail(store.getDetail())
                                .build())
                        .image(store.getImageUrl())
                        .myStampCount(getMyStampCount(store)) // ← 이 부분은 로그인 정보 필요 시 수정
                        .rewards(getRewardInfoList(store))
                        .build())
                .collect(Collectors.toList());
    }

    private int getMyStampCount(Store store) {
        // TODO: 로그인 사용자 ID 필요 시 SecurityContext 등에서 가져와야 함
        return 0; // 일단 더미값
    }

    private List<StoreResponse.RewardInfo> getRewardInfoList(Store store) {
        List<Reward> rewards = deomRepository.findByStoreId(store.getId());

        return rewards.stream()
                .map(r -> StoreResponse.RewardInfo.builder()
                        .deomId(r.getId())
                        .name(r.getName())
                        .requiredStampAmount(r.getRequiredStampAmount())
                        .status(r.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    private StoreResponse toStoreResponse(Store store) {
        return StoreResponse.builder()
                .storeId(store.getId())
                .storeName(store.getName())
                .branchName(store.getBranch())
                .address(StoreResponse.Address.builder()
                        .city(store.getCity())
                        .street(store.getStreet())
                        .detail(store.getDetail())
                        .build())
                .image(store.getImageUrl())
                .myStampCount(getMyStampCount(store)) // TODO: 로그인 사용자 기반 처리
                .rewards(getRewardInfoList(store))
                .build();
    }

        */

}
