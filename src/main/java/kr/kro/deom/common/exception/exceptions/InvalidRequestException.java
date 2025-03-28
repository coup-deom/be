package kr.kro.deom.common.exception.exceptions;

import kr.kro.deom.common.exception.messages.InvalidRequestMessages;
import org.springframework.http.HttpStatus;

public class InvalidRequestException extends CustomException {
    public InvalidRequestException(InvalidRequestMessages message) {
        super(message.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
