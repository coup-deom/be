package kr.kro.deom.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonSuccessCode implements BaseResponseCode {
    OK(HttpStatus.OK, "C000", "요청이 정상적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "C001", "리소스가 생성되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
