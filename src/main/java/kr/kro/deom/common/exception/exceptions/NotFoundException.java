package kr.kro.deom.common.exception.exceptions;

import kr.kro.deom.common.exception.messages.NotFoundMessages;
import org.springframework.http.HttpStatus;

public class NotFoundException extends CustomException {
  public NotFoundException(NotFoundMessages message) {
    super(message.getMessage(), HttpStatus.NOT_FOUND);
  }
}
