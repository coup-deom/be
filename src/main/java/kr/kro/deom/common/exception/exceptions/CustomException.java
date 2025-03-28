package kr.kro.deom.common.exception.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException{
    private final HttpStatus status;

    public CustomException(final String message, final HttpStatus status) {
        super(message);
        this.status = status;
    }
}
