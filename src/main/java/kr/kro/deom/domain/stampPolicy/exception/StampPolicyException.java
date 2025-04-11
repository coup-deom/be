package kr.kro.deom.domain.stampPolicy.exception;

import kr.kro.deom.common.exception.exceptions.CustomException;
import kr.kro.deom.common.response.BaseResponseCode;

public class StampPolicyException extends CustomException {
    public StampPolicyException(BaseResponseCode code) {
        super(code);
    }

    public StampPolicyException(BaseResponseCode code, String message) {
        super(code, message);
    }
}
