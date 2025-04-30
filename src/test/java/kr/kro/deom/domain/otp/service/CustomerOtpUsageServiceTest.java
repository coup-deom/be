package kr.kro.deom.domain.otp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import kr.kro.deom.domain.deom.entity.Deom;
import kr.kro.deom.domain.deom.service.DeomService;
import kr.kro.deom.domain.otp.dto.response.OtpUsageResponse;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpType;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import kr.kro.deom.domain.store.entity.Store;
import kr.kro.deom.domain.store.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerOtpUsageServiceTest {

    @Mock private OtpRepository otpRepository;

    @Mock private StoreService storeService;

    @Mock private DeomService deomService;

    @InjectMocks private CustomerOtpUsageService customerOtpUsageService;

    private OtpUsage otpUsage1;
    private OtpUsage otpUsage2;
    private Store store;
    private Deom deom;

    @BeforeEach
    void setUp() {
        otpUsage1 =
                OtpUsage.builder()
                        .id(1L)
                        .userId(1L)
                        .storeId(100L)
                        .type(OtpType.STAMP)
                        .status(OtpStatus.PENDING)
                        .usedStampAmount(5)
                        .otp(1234L)
                        .build();

        otpUsage2 =
                OtpUsage.builder()
                        .id(2L)
                        .userId(1L)
                        .storeId(100L)
                        .type(OtpType.DEOM)
                        .status(OtpStatus.APPROVED)
                        .usedStampAmount(3)
                        .otp(1235L)
                        .deomId(200L)
                        .build();

        store = Store.builder().id(100L).storeName("Store1").build();

        deom = Deom.builder().id(200L).name("Deom1").build();
    }

    @Test
    @DisplayName("전체 OTP 사용 이력 조회")
    void getAllUsagesTest() {
        when(otpRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(otpUsage1, otpUsage2));
        when(storeService.getStore(100L)).thenReturn(store);
        when(deomService.getDeom(200L)).thenReturn(deom);

        List<OtpUsageResponse> responses = customerOtpUsageService.getAllUsages(1L);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("otpId").containsExactly(1L, 2L);
        assertThat(responses)
                .extracting("status")
                .containsExactly(OtpStatus.PENDING, OtpStatus.APPROVED);

        verify(otpRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L);
        verify(storeService, times(2)).getStore(100L);
    }

    @Test
    @DisplayName("특정 상태의 OTP 사용 이력 조회")
    void getUsagesByStatusTest() {
        when(otpRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(eq(1L), anyList()))
                .thenReturn(List.of(otpUsage1));
        when(storeService.getStore(100L)).thenReturn(store);

        List<OtpUsageResponse> responses =
                customerOtpUsageService.getUsagesByStatus(1L, List.of(OtpStatus.PENDING));

        assertThat(responses).hasSize(1);
        OtpUsageResponse response = responses.get(0);
        assertThat(response.getStatus()).isEqualTo(OtpStatus.PENDING);

        verify(otpRepository, times(1))
                .findByUserIdAndStatusInOrderByCreatedAtDesc(eq(1L), anyList());
        verify(storeService, times(1)).getStore(100L);
    }

    @Test
    @DisplayName("DEOM 타입 OTP 사용 이력 조회시 Deom 정보 포함")
    void getAllUsagesWithDeomTest() {

        when(otpRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(otpUsage2));
        when(storeService.getStore(100L)).thenReturn(store);
        when(deomService.getDeom(200L)).thenReturn(deom);

        List<OtpUsageResponse> responses = customerOtpUsageService.getAllUsages(1L);

        assertThat(responses).hasSize(1);
        OtpUsageResponse response = responses.get(0);
        assertThat(response.getDeomId()).isEqualTo(200L);
        assertThat(response.getDeomName()).isEqualTo("Deom1");

        verify(deomService, times(1)).getDeom(200L);
    }
}
