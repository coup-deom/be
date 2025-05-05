package kr.kro.deom.domain.store.dto.response;

import kr.kro.deom.domain.store.entity.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreSelectResponse {

    private Long storeId;
    private String storeName;
    private String branchName;

    public static StoreSelectResponse from(Store store) {
        return StoreSelectResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .branchName(store.getBranchName())
                .build();
    }
}
