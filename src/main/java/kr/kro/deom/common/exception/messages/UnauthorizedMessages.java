package kr.kro.deom.common.exception.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UnauthorizedMessages {
  ACCESS_TOKEN_EXPIRED("액세스 토큰이 만료되었습니다."),
  INVALID_ACCESS_TOKEN("유효하지 않은 액세스 토큰입니다."),
  NOT_FOUND_COOKIE("쿠키를 찾을 수 없습니다."),
  NOT_FOUND_REFRESH_TOKEN("리프레쉬 토큰을 찾을 수 없습니다."),
  NOT_FOUND_ACCESS_TOKEN("리프레쉬 토큰을 찾을 수 없습니다."),
  REFRESH_TOKEN_EXPIRED("리프레쉬 토큰이 만료되었습니다."),
  INVALID_REFRESH_TOKEN("유효하지 않은 리프레쉬 토큰입니다."),
  AUTHENTICATION_REQUIRED("로그인 후 다시 시도해주세요."),
  AUTHENTICATION_FAILED("인증에 실패하였습니다."),
  ;

  private final String message;
}
