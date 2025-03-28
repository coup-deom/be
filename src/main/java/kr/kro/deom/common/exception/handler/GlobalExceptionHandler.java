package kr.kro.deom.common.exception.handler;

import kr.kro.deom.common.exception.exceptions.CustomException;
import kr.kro.deom.common.exception.response.GlobalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<GlobalResponse<Void>> handleCustomException(CustomException e) {
        log.error("Custom exception: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(GlobalResponse.fail(e));
    }

    // Validation 관련 예외 처리 - 필드별 에러 메시지 반환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = Optional.ofNullable(error.getDefaultMessage())
                    .orElse("Invalid value");
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalResponse.fail("입력값 검증에 실패했습니다.", errors));
    }

    // JSON 파싱 예외 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e) {
        log.error("Message not readable: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalResponse.fail("잘못된 형식의 JSON 요청입니다."));
    }

    // 필수 파라미터 누락 예외 처리
    @ExceptionHandler({
            MissingServletRequestPartException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<GlobalResponse<Void>> handleMissingServletRequestPart(Exception e) {
        log.error("Missing required parameter: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalResponse.fail("필수 파라미터가 누락되었습니다: " +
                        e.getMessage().replaceAll("^.*\\['|'\\].*$", "")));
    }

    // 타입 불일치 예외 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GlobalResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException e) {
        log.error("Type mismatch: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalResponse.fail(String.format("파라미터 '%s'의 타입이 잘못되었습니다. %s",
                        e.getName(),
                        Optional.ofNullable(e.getRequiredType())
                                .map(Class::getSimpleName)
                                .map(type -> "'" + type + "' 타입이 필요합니다.")
                                .orElse("올바른 타입이 필요합니다."))));
    }

    // 기타 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<Void>> handleUncaughtException(Exception e) {
        log.error("Uncaught exception", e); // 전체 스택 트레이스 로깅
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalResponse.fail("서버 내부 오류가 발생했습니다."));
    }
}