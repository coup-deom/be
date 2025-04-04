package kr.kro.deom.domain.auth.exception;

import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.exception.exceptions.CustomException;

public class RefreshTokenExpiredException extends CustomException {
    public RefreshTokenExpiredException() {
        super(CommonErrorCode.REFRESH_TOKEN_EXPIRED);
    }
}