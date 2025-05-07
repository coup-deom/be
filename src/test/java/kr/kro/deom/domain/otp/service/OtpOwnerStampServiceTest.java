package kr.kro.deom.domain.otp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import kr.kro.deom.domain.myStamp.entity.MyStamp;
import kr.kro.deom.domain.myStamp.exception.MyStampException;
import kr.kro.deom.domain.myStamp.repository.MyStampRepository;
import kr.kro.deom.domain.otp.dto.OtpRedisDto;
import kr.kro.deom.domain.otp.dto.request.OtpStampApproveRequest;
import kr.kro.deom.domain.otp.dto.response.OwnerStampInfoResponse;
import kr.kro.deom.domain.otp.repository.OtpRepository;
import kr.kro.deom.domain.stampPolicy.dto.StampPolicyDto;
import kr.kro.deom.domain.stampPolicy.service.StampPolicyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OtpOwnerStampServiceTest {

    @Mock private OtpRepository otpRepository;

    @Mock private MyStampRepository myStampRepository;

    @Mock private OtpRedisService otpRedisService;

    @Mock private StampPolicyService stampPolicyService;

    @Mock private OtpOwnerService otpOwnerService;

    @InjectMocks private OtpOwnerStampService otpOwnerStampService;

    private final Long TEST_OTP_CODE = 1234L;
    private final Long TEST_STORE_ID = 1L;
    private final Long TEST_USER_ID = 10L;
    private final Integer TEST_STAMP_AMOUNT = 5;

    @Test
    @DisplayName("스탬프 정보 및 정책 조회 성공")
    void getUserStampStatusAndStampPolicy_Success() {
        // given
        OtpRedisDto otpRedisDto =
                OtpRedisDto.builder().userId(TEST_USER_ID).storeId(TEST_STORE_ID).build();

        List<StampPolicyDto> policyList =
                List.of(new StampPolicyDto(1L, 1000, 1), new StampPolicyDto(2L, 2000, 2));

        // when
        when(otpRedisService.getOtpFromRedis(TEST_OTP_CODE, TEST_STORE_ID)).thenReturn(otpRedisDto);
        when(myStampRepository.findStampAmountByUserIdAndStoreId(TEST_USER_ID, TEST_STORE_ID))
                .thenReturn(3);
        when(stampPolicyService.getStampPolicy(TEST_STORE_ID)).thenReturn(policyList);

        OwnerStampInfoResponse response =
                otpOwnerStampService.getUserStampStatusAndStampPolicy(TEST_OTP_CODE, TEST_STORE_ID);

        // then
        assertNotNull(response);
        assertEquals(3, response.getCustomerStampAmount());
        assertEquals(2, response.getStampPolicyList().size());
        assertEquals(1000, response.getStampPolicyList().get(0).baseAmount());
        assertEquals(2000, response.getStampPolicyList().get(1).baseAmount());

        verify(otpRedisService).getOtpFromRedis(TEST_OTP_CODE, TEST_STORE_ID);
        verify(myStampRepository).findStampAmountByUserIdAndStoreId(TEST_USER_ID, TEST_STORE_ID);
        verify(stampPolicyService).getStampPolicy(TEST_STORE_ID);
    }

    @Test
    @DisplayName("고객 스탬프가 없는 경우 0으로 반환")
    void getUserStampStatusAndStampPolicy_NoStamps() {
        // given
        OtpRedisDto otpRedisDto =
                OtpRedisDto.builder().userId(TEST_USER_ID).storeId(TEST_STORE_ID).build();

        List<StampPolicyDto> policyList = List.of(new StampPolicyDto(1L, 1000, 1));

        // when
        when(otpRedisService.getOtpFromRedis(TEST_OTP_CODE, TEST_STORE_ID)).thenReturn(otpRedisDto);
        when(myStampRepository.findStampAmountByUserIdAndStoreId(TEST_USER_ID, TEST_STORE_ID))
                .thenReturn(null);
        when(stampPolicyService.getStampPolicy(TEST_STORE_ID)).thenReturn(policyList);

        OwnerStampInfoResponse response =
                otpOwnerStampService.getUserStampStatusAndStampPolicy(TEST_OTP_CODE, TEST_STORE_ID);

        // then
        assertNotNull(response);
        assertEquals(0, response.getCustomerStampAmount());
    }

    @Test
    @DisplayName("스탬프 적립 승인 성공")
    void approveOtpAndAddStamp_Success() {
        // given
        OtpStampApproveRequest request = new OtpStampApproveRequest();
        ReflectionTestUtils.setField(request, "userId", TEST_USER_ID);
        ReflectionTestUtils.setField(request, "storeId", TEST_STORE_ID);
        ReflectionTestUtils.setField(request, "otpCode", TEST_OTP_CODE);
        ReflectionTestUtils.setField(request, "amount", TEST_STAMP_AMOUNT);

        // when
        when(myStampRepository.incrementStamp(TEST_USER_ID, TEST_STORE_ID, TEST_STAMP_AMOUNT))
                .thenReturn(1);

        otpOwnerStampService.approveOtpAndAddStamp(request);

        // then
        verify(otpOwnerService).approveOtp(TEST_OTP_CODE, TEST_USER_ID, TEST_STORE_ID);
        verify(myStampRepository).incrementStamp(TEST_USER_ID, TEST_STORE_ID, TEST_STAMP_AMOUNT);
    }

    @Test
    @DisplayName("스탬프 적립이 없는 경우 새로 생성")
    void approveOtpAndAddStamp_CreateNew() {
        // given
        OtpStampApproveRequest request = new OtpStampApproveRequest();
        ReflectionTestUtils.setField(request, "userId", TEST_USER_ID);
        ReflectionTestUtils.setField(request, "storeId", TEST_STORE_ID);
        ReflectionTestUtils.setField(request, "otpCode", TEST_OTP_CODE);
        ReflectionTestUtils.setField(request, "amount", TEST_STAMP_AMOUNT);

        // when
        when(myStampRepository.incrementStamp(TEST_USER_ID, TEST_STORE_ID, TEST_STAMP_AMOUNT))
                .thenReturn(0);

        otpOwnerStampService.approveOtpAndAddStamp(request);

        // then
        verify(otpOwnerService).approveOtp(TEST_OTP_CODE, TEST_USER_ID, TEST_STORE_ID);
        verify(myStampRepository).incrementStamp(TEST_USER_ID, TEST_STORE_ID, TEST_STAMP_AMOUNT);
        verify(myStampRepository).save(any(MyStamp.class));
    }

    @Test
    @DisplayName("스탬프 수량이 0 이하인 경우 예외 발생")
    void approveOtpAndAddStamp_InvalidAmount() {
        // given
        OtpStampApproveRequest request = new OtpStampApproveRequest();
        ReflectionTestUtils.setField(request, "userId", TEST_USER_ID);
        ReflectionTestUtils.setField(request, "storeId", TEST_STORE_ID);
        ReflectionTestUtils.setField(request, "otpCode", TEST_OTP_CODE);
        ReflectionTestUtils.setField(request, "amount", 0);

        // then
        assertThrows(
                MyStampException.class, () -> otpOwnerStampService.approveOtpAndAddStamp(request));
    }

    @Test
    @DisplayName("스탬프 적립 요청 거절 성공")
    void rejectStampOtp_Success() {
        // given
        OtpStampApproveRequest request = new OtpStampApproveRequest();
        ReflectionTestUtils.setField(request, "userId", TEST_USER_ID);
        ReflectionTestUtils.setField(request, "storeId", TEST_STORE_ID);
        ReflectionTestUtils.setField(request, "otpCode", TEST_OTP_CODE);
        ReflectionTestUtils.setField(request, "amount", TEST_STAMP_AMOUNT);

        // when
        otpOwnerStampService.rejectStampOtp(request);

        // then
        verify(otpOwnerService).rejectOtp(TEST_OTP_CODE, TEST_USER_ID, TEST_STORE_ID);
    }
}
