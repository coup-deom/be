package kr.kro.deom.common.exception.handler;

import kr.kro.deom.common.exception.exceptions.CustomException;
import kr.kro.deom.common.exception.response.GlobalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //custom exception
    @ExceptionHandler({CustomException.class})
    public ResponseEntity<GlobalResponse<Void>> handleCustomException(CustomException e){
        log.error("message : {}", e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(GlobalResponse.fail(e));
    }

    //처리되지 못한 기타 exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<Void>> handlingException(Exception e) {
        log.error("MESSAGE : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GlobalResponse.fail(e.getMessage()));
    }

    //request 관련 error
    @ExceptionHandler({
            MethodArgumentNotValidException.class, //json body (requestpart의 body, requestBody의 body)의 필드가 설정한 유효값을 만족시키지 않거나, 필수값이 누락됨.
            HttpMessageNotReadableException.class, //json body (requestpart의 body, requestBody의 body)의 필드 type이 잘못됨.
            MissingServletRequestPartException.class,   // required인 requestpart가 없음.
            MissingServletRequestParameterException.class, // requried인 request param이 없음.
            MethodArgumentTypeMismatchException.class //request parameter, pathVariable의 type이 잘못됨.
    })
    public ResponseEntity<GlobalResponse<Void>> handleMissingServletRequestPartException(Exception e){
        log.error("MESSAGE : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GlobalResponse.fail(e.getMessage()));
    }
}