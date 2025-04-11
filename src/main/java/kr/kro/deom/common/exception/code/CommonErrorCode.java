package kr.kro.deom.common.exception.code;

import kr.kro.deom.common.response.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements BaseResponseCode {

    // common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값 검증에 실패했습니다."),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "C002", "잘못된 형식의 JSON 요청입니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "C003", "필수 파라미터가 누락되었습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "C004", "요청 파라미터 타입이 잘못되었습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C005", "서버 내부 오류가 발생했습니다."),

    // auth
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A001", "리프레쉬 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 리프레쉬 토큰입니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A003", "인증에 실패하였습니다."),

    // user
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 사용자입니다."),

    // otp
    OPT_EXPIRED(HttpStatus.BAD_REQUEST, "O001", "OTP가 존재하지 만료되었습니다."),
    OTP_INVALID(HttpStatus.NOT_FOUND, "O002", "존재하지 않는 OTP입니다."),
    OPT_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "O003", "이미 처리된 OTP입니다."),
    OTP_UNAUTHORIZED(HttpStatus.FORBIDDEN, "O004", "이 OTP에 대한 권한이 없습니다."),

    // stamp
    INVALID_STAMP_AMOUNT(HttpStatus.BAD_REQUEST, "AMOUNT_001", "수량은 1 이상이어야 합니다."),

    // stamp policy
    INVALID_BASE_AMOUNT(HttpStatus.BAD_REQUEST, "POLICY_001", "기준 금액은 0보다 커야 합니다."),
    INVALID_STAMP_COUNT(HttpStatus.BAD_REQUEST, "POLICY_002", "스탬프 개수는 0보다 커야 합니다."),
    ALREADY_REGISTERED_STAMP_POLICY(HttpStatus.CONFLICT, "POLICY_003", "이미 등록된 스탬프 정책이 있습니다."),
    INVALID_STAMP_POLICY(HttpStatus.NOT_FOUND, "POLICY_004", "존재하지 않는 스탬프 정책입니다."),

    // store
    DUPLICATE_BUSINESS_NUMBER(HttpStatus.CONFLICT, "STORE_001", "이미 등록된 사업자번호입니다."),
    DUPLICATE_STORE_NAME_AND_BRANCH(HttpStatus.CONFLICT, "STORE_002", "이미 등록된 가게명과 지점명입니다."),
    NO_PERMISSION_FOR_STORE(HttpStatus.FORBIDDEN, "STORE_003", "이 가게에 대한 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
