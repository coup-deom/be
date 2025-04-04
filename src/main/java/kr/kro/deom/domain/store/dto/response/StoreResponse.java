package kr.kro.deom.domain.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StoreResponse {

    private Long storeId;
    private String storeName;
    private String branchName;
    private Address address;
    private String image;
    private int myStampCount;
    private List<RewardInfo> rewards;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Address {
        private String city;
        private String street;
        private String detail;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RewardInfo {
        private Long deomId;
        private String name;
        private int requiredStampAmount;
        private DeomStatus status;
    }

}
