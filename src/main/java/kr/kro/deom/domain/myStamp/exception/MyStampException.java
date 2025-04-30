package kr.kro.deom.domain.myStamp.exception;

import kr.kro.deom.common.exception.exceptions.CustomException;
import kr.kro.deom.common.response.BaseResponseCode;

public class MyStampException extends CustomException {
    public MyStampException(BaseResponseCode code) {
        super(code);
    }

    public MyStampException(BaseResponseCode code, String message) {
        super(code, message);
    }
}
