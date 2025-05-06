package kr.kro.deom.domain.exchange.exception;

import kr.kro.deom.common.exception.exceptions.CustomException;
import kr.kro.deom.common.response.BaseResponseCode;

public class StampExchangeException extends CustomException {
    public StampExchangeException(BaseResponseCode code) {
        super(code);
    }
}
