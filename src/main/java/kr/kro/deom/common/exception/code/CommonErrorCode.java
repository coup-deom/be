package kr.kro.deom.common.exception.code;

import kr.kro.deom.common.response.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements BaseResponseCode {
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값 검증에 실패했습니다."),
  JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "C002", "잘못된 형식의 JSON 요청입니다."),
  MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "C003", "필수 파라미터가 누락되었습니다."),
  TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "C004", "요청 파라미터 타입이 잘못되었습니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C005", "서버 내부 오류가 발생했습니다."),

  // user
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 사용자입니다.");
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}
