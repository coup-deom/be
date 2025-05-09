package kr.kro.deom.domain.store.dto.response;

import kr.kro.deom.domain.store.entity.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoreStatusResponse {
    private StoreStatus status;
}
