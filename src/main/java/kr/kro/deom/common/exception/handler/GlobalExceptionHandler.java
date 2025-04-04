package kr.kro.deom.common.exception.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.exception.exceptions.CustomException;
import kr.kro.deom.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // CustomException 처리
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
    log.error("Custom exception: {}", e.getMessage());
    return ResponseEntity.status(e.getBaseResponseCode().getStatus())
        .body(ApiResponse.fail(e.getBaseResponseCode()));
  }

  // Validation 예외 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
      MethodArgumentNotValidException e) {
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage =
                  Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid value");
              errors.put(fieldName, errorMessage);
            });

    log.error("Validation error: {}", errors);
    return ResponseEntity.status(CommonErrorCode.INVALID_INPUT_VALUE.getStatus())
        .body(ApiResponse.fail(CommonErrorCode.INVALID_INPUT_VALUE));
  }

  // JSON 파싱 오류
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
      HttpMessageNotReadableException e) {
    log.error("Message not readable: {}", e.getMessage());
    return ResponseEntity.status(CommonErrorCode.JSON_PARSE_ERROR.getStatus())
        .body(ApiResponse.fail(CommonErrorCode.JSON_PARSE_ERROR));
  }

  // 필수 파라미터 누락
  @ExceptionHandler({
    MissingServletRequestPartException.class,
    MissingServletRequestParameterException.class
  })
  public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestPart(Exception e) {
    log.error("Missing parameter: {}", e.getMessage());
    return ResponseEntity.status(CommonErrorCode.MISSING_PARAMETER.getStatus())
        .body(ApiResponse.fail(CommonErrorCode.MISSING_PARAMETER));
  }

  // 타입 불일치
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
      MethodArgumentTypeMismatchException e) {
    log.error("Type mismatch: {}", e.getMessage());
    return ResponseEntity.status(CommonErrorCode.TYPE_MISMATCH.getStatus())
        .body(ApiResponse.fail(CommonErrorCode.TYPE_MISMATCH));
  }

  // 예상하지 못한 모든 예외
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUncaughtException(Exception e) {
    log.error("Uncaught exception", e);
    return ResponseEntity.status(CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus())
        .body(ApiResponse.fail(CommonErrorCode.INTERNAL_SERVER_ERROR));
  }
}
