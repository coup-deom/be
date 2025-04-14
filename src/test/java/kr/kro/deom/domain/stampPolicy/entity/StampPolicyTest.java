package kr.kro.deom.domain.stampPolicy.entity;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.stampPolicy.exception.StampPolicyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StampPolicyTest {

    @Test
    @DisplayName("스탬프 정책 생성 성공")
    void createPolicy_success() {
        // given
        Long storeId = 1L;
        int baseAmount = 10000;
        int stampCount = 5;

        // when
        StampPolicy policy = StampPolicy.create(storeId, baseAmount, stampCount);

        // then
        assertThat(policy.getStoreId()).isEqualTo(storeId);
        assertThat(policy.getBaseAmount()).isEqualTo(baseAmount);
        assertThat(policy.getStampCount()).isEqualTo(stampCount);
        assertThat(policy.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("기준 금액이 0 이하일 경우 예외 발생")
    void createPolicy_fail_invalidBaseAmount() {
        // when
        StampPolicyException exception =
                assertThrows(StampPolicyException.class, () -> StampPolicy.create(1L, 0, 1));

        // then
        assertThat(exception.getBaseResponseCode()).isEqualTo(CommonErrorCode.INVALID_BASE_AMOUNT);
    }

    @Test
    @DisplayName("스탬프 개수가 0 이하일 경우 예외 발생")
    void createPolicy_fail_invalidStampCount() {
        // when
        StampPolicyException exception =
                assertThrows(StampPolicyException.class, () -> StampPolicy.create(1L, 10000, -1));

        // then
        assertThat(exception.getBaseResponseCode()).isEqualTo(CommonErrorCode.INVALID_STAMP_COUNT);
    }

    @Test
    @DisplayName("스탬프 정책 수정")
    void updatePolicy_success() {
        // given
        StampPolicy policy = StampPolicy.create(1L, 10000, 5);

        // when
        policy.update(15000, 10);

        // then
        assertThat(policy.getBaseAmount()).isEqualTo(15000);
        assertThat(policy.getStampCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("정책 삭제(markAsDeleted) 호출 시 deletedAt이 설정되어야 한다")
    void markAsDeleted_success() {
        // given
        StampPolicy policy = StampPolicy.create(1L, 10000, 5);

        // when
        policy.markAsDeleted();

        // then
        assertThat(policy.getDeletedAt()).isNotNull();
        assertThat(policy.getDeletedAt()).isBeforeOrEqualTo(Instant.now());
    }
}
