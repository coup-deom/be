package kr.kro.deom.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({"isSuccess", "status", "code", "message", "data"})
public class ApiResponse<T> {

  @JsonProperty("isSuccess")
  private final boolean isSuccess;

  private final String status; // ex) OK, BAD_REQUEST
  private final String code; // ex) U001, A002
  private final String message; // 한글 메시지

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final T data;

  public static <T> ApiResponse<T> success(BaseResponseCode code, T data) {
    return new ApiResponse<>(
        true, code.getStatus().name(), code.getCode(), code.getMessage(), data);
  }

  public static ApiResponse<Void> success(BaseResponseCode code) {
    return new ApiResponse<>(
        true, code.getStatus().name(), code.getCode(), code.getMessage(), null);
  }

  public static ApiResponse<Void> fail(BaseResponseCode code) {
    return new ApiResponse<>(
        false, code.getStatus().name(), code.getCode(), code.getMessage(), null);
  }
}
