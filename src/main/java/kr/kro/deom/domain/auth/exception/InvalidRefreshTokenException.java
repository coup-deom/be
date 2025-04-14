package kr.kro.deom.domain.auth.exception;

import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.exception.exceptions.CustomException;

public class InvalidRefreshTokenException extends CustomException {
    public InvalidRefreshTokenException() {
        super(CommonErrorCode.INVALID_REFRESH_TOKEN);
    }
}
