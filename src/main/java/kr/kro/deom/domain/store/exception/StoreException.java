package kr.kro.deom.domain.store.exception;

import kr.kro.deom.common.exception.exceptions.CustomException;
import kr.kro.deom.common.response.BaseResponseCode;

public class StoreException extends CustomException {
    public StoreException(BaseResponseCode code) {
        super(code);
    }

    public StoreException(BaseResponseCode code, String message) {
        super(code, message);
    }
}
