package kr.kro.deom.domain.otp.exception;

import kr.kro.deom.common.exception.exceptions.CustomException;
import kr.kro.deom.common.response.BaseResponseCode;

public class OtpException extends CustomException {
    public OtpException(BaseResponseCode code) {
        super(code);
    }

    public OtpException(BaseResponseCode code, String message) {
        super(code, message);
    }
}
