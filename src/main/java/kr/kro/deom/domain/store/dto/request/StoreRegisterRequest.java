package kr.kro.deom.domain.store.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreRegisterRequest {

    @NotNull(message = "유저 ID는 필수입니다.")
    private Long ownerId;

    @NotNull(message = "사업자번호는 필수입니다.")
    private Long businessNumber;

    @NotBlank(message = "가게 이름은 필수입니다.")
    private String storeName;

    private String branchName;

    @NotBlank(message = "주소(시)는 필수입니다.")
    private String addressCity;

    @NotBlank(message = "주소(구/동)는 필수입니다.")
    private String addressStreet;

    private String addressDetail;

    private String image;
}
