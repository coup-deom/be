package kr.kro.deom.common.exception.exceptions;

import kr.kro.deom.common.response.BaseResponseCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final BaseResponseCode baseResponseCode;

    public CustomException(BaseResponseCode code, String message) {
        super(message);
        this.baseResponseCode = code;
    }

    public CustomException(BaseResponseCode code) {
        super(" ");
        this.baseResponseCode = code;
    }
}
