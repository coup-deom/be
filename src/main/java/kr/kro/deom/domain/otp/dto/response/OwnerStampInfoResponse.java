package kr.kro.deom.domain.otp.dto.response;

import java.util.List;
import kr.kro.deom.domain.stampPolicy.dto.StampPolicyDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerStampInfoResponse {
    private int customerStampAmount;
    private List<StampPolicyDto> stampPolicyList;
}
