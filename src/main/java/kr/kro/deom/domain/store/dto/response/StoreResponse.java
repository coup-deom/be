package kr.kro.deom.domain.store.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StoreResponse {

  private Long storeId;
  private String storeName;
  private String branchName;
  private String image;
  private int myStampCount;
  private List<DeomInfo> deoms;
  private String city;
  private String street;
  private String detail;

  @Builder
  @Getter
  @AllArgsConstructor
  public static class DeomInfo {
    private Long deomId;
    private String name;
    private int requiredStampAmount;
    private DeomStatus status;
  }
}
