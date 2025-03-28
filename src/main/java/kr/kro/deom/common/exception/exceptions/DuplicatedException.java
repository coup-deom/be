package kr.kro.deom.common.exception.exceptions;

import kr.kro.deom.common.exception.messages.DuplicatedMessages;
import org.springframework.http.HttpStatus;

public class DuplicatedException extends CustomException {
    public DuplicatedException(DuplicatedMessages message) {
        super(message.getMessage(), HttpStatus.CONFLICT);
    }
}
