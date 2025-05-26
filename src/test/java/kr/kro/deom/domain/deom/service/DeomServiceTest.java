package kr.kro.deom.domain.deom.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import kr.kro.deom.domain.deom.dto.DeomDto;
import kr.kro.deom.domain.deom.entity.Deom;
import kr.kro.deom.domain.deom.repository.DeomRepository;
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
class DeomServiceTest {

    @InjectMocks private DeomService deomService;

    @Mock private DeomRepository deomRepository;

    @Mock private StoreRepository storeRepository;

    private final Long userId = 1L;
    private final Long storeId = 1L;
    private final Long deomId = 1L;
    private final String deomName = "테스트 디옴";
    private final int requiredStampAmount = 10;

    private Store store;
    private Deom deom;
    private List<DeomDto> deomDtoList;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        store = new Store(); // 가정: Store 클래스가 기본 생성자를 가지고 있음

        deom = Deom.create(storeId, deomName, requiredStampAmount);

        DeomDto deomDto = new DeomDto(deomId, deomName, requiredStampAmount);
        deomDtoList = List.of(deomDto);
    }

    @Test
    @DisplayName("덤 정책 목록 조회 성공")
    void getDeomPolicy_Success() {
        // given
        when(deomRepository.findPoliciesByStoreIdAndDeletedAtIsNull(storeId))
                .thenReturn(deomDtoList);

        // when
        List<DeomDto> result = deomService.getDeomPolicy(storeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).name()).isEqualTo(deomName);
        assertThat(result.get(0).requiredStampAmount()).isEqualTo(requiredStampAmount);

        verify(deomRepository).findPoliciesByStoreIdAndDeletedAtIsNull(storeId);
    }
}
