package kr.kro.deom.domain.user.exception;

import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.exception.exceptions.CustomException;

public class UserNotFoundException extends CustomException {
  public UserNotFoundException() {
    super(CommonErrorCode.USER_NOT_FOUND);
  }
}
