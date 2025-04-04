package kr.kro.deom.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({"isSuccess", "status", "code", "data"})
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private Boolean isSuccess;

    @JsonProperty("status")
    private String status;

    @JsonProperty("code")
    private String code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("data")
    private T data;

    public static <T> ApiResponse<T> success(BaseResponseCode code, T data) {
        return new ApiResponse<>(true, code.getStatus().name(), code.getCode(), data);
    }

    public static <T> ApiResponse<T> success(BaseResponseCode code) {
        return success(code, null);
    }

    public static <T> ApiResponse<T> fail(BaseResponseCode code, T data) {
        return new ApiResponse<>(false, code.getStatus().name(), code.getCode(), data);
    }

    public static <T> ApiResponse<T> fail(BaseResponseCode code) {
        return fail(code, null);
    }

    public static <T> ApiResponse<T> success(HttpStatus status, T data) {
        return new ApiResponse<>(true, status.name(), String.valueOf(status.value()), data);
    }

}

