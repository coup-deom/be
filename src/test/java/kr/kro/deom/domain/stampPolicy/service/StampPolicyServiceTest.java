package kr.kro.deom.domain.stampPolicy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import kr.kro.deom.domain.stampPolicy.dto.*;
import kr.kro.deom.domain.stampPolicy.entity.StampPolicy;
import kr.kro.deom.domain.stampPolicy.repository.StampPolicyRepository;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StampPolicyServiceTest {

    @InjectMocks private StampPolicyService stampPolicyService;

    @Mock private StoreRepository storeRepository;

    @Mock private StampPolicyRepository stampPolicyRepository;

    private final Long userId = 1L;
    private final Long storeId = 1L;
    private final Long policyId = 1L;
    private final int baseAmount = 10000;
    private final int stampCount = 10;

    private Store store;
    private StampPolicy stampPolicy;
    private List<StampPolicyDto> policyDtoList;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        store = new Store(); // 가정: Store 클래스가 기본 생성자를 가지고 있음

        stampPolicy = StampPolicy.create(storeId, baseAmount, stampCount);

        StampPolicyDto policyDto = new StampPolicyDto(policyId, baseAmount, stampCount);
        policyDtoList = List.of(policyDto);
    }

    @Test
    @DisplayName("스탬프 정책 목록 조회 성공")
    void getStampPolicy_Success() {
        // given
        when(stampPolicyRepository.findPoliciesByStoreId(storeId)).thenReturn(policyDtoList);

        // when
        List<StampPolicyDto> result = stampPolicyService.getStampPolicy(storeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).baseAmount()).isEqualTo(baseAmount);
        assertThat(result.get(0).stampCount()).isEqualTo(stampCount);

        verify(stampPolicyRepository).findPoliciesByStoreId(storeId);
    }
}
