package kr.kro.deom.domain.exception;

import kr.kro.deom.common.exception.exceptions.CustomException;
import kr.kro.deom.common.response.BaseResponseCode;

public class DeomException extends CustomException {
    public DeomException(BaseResponseCode code) {
        super(code);
    }

    public DeomException(BaseResponseCode code, String message) {
        super(code, message);
    }
}
