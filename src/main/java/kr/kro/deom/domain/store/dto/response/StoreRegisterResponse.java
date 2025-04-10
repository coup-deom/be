package kr.kro.deom.domain.store.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreRegisterResponse {
    private Long ownerId;
    private Long businessNumber;
    private String storeName;
    private String branchName;
    private String addressCity;
    private String addressStreet;
    private String addressDetail;
    private String image;
}
