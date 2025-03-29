package kr.kro.deom.common.exception.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Map;
import kr.kro.deom.common.exception.exceptions.CustomException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonPropertyOrder({"isSuccess", "code", "message", "data"})
public class GlobalResponse<T> {

  @JsonProperty("isSuccess")
  private boolean success;

  private HttpStatus code;

  private String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data;

  public static <T> GlobalResponse<T> success(T data) {
    return new GlobalResponse<>(true, HttpStatus.OK, "요청에 성공하였습니다.", data);
  }

  public static <T> GlobalResponse<T> created(T data) {
    return new GlobalResponse<>(true, HttpStatus.CREATED, "리소스가 성공적으로 생성되었습니다.", data);
  }

  public static <T> GlobalResponse<T> fail(CustomException e) {
    return new GlobalResponse<>(false, e.getStatus(), e.getMessage(), null);
  }

  public static <T> GlobalResponse<T> fail(String message) {
    return new GlobalResponse<>(false, HttpStatus.BAD_REQUEST, message, null);
  }

  public static <K, V> GlobalResponse<Map<K, V>> fail(String message, Map<K, V> errors) {
    return new GlobalResponse<>(false, HttpStatus.BAD_REQUEST, message, errors);
  }
}
