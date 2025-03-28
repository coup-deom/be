package kr.kro.deom.common.exception.exceptions;

import kr.kro.deom.common.exception.messages.UnauthorizedMessages;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends CustomException {
    public UnauthorizedException(UnauthorizedMessages message) {
        super(message.getMessage(), HttpStatus.UNAUTHORIZED);
    }
}
