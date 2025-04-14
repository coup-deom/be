package kr.kro.deom.common.response;

import org.springframework.http.HttpStatus;

public interface BaseResponseCode {
    HttpStatus getStatus();

    String getCode();

    String getMessage();
}
