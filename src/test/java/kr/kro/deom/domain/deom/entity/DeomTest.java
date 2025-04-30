package kr.kro.deom.domain.deom.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.domain.deom.exception.DeomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DeomTest {
    @Test
    @DisplayName("덤 이름이 null이면 예외 발생")
    void throwWhenNameIsNull() {
        DeomException exception = assertThrows(DeomException.class, () -> Deom.create(1L, null, 5));

        assertThat(exception.getBaseResponseCode()).isEqualTo(CommonErrorCode.INVALID_DEOM_NAME);
    }

    @Test
    @DisplayName("필요 스탬프 개수가 0 이하면 예외 발생")
    void throwWhenRequiredStampAmountIsInvalid() {
        DeomException exception = assertThrows(DeomException.class, () -> Deom.create(1L, "이름", 0));

        assertThat(exception.getBaseResponseCode())
                .isEqualTo(CommonErrorCode.INVALID_REQUIRED_STAMP_AMOUNT);
    }
}
