package kr.kro.deom.common.exception.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DuplicatedMessages {
    EMAIL("이미 등록된 이메일입니다.");
    private final String message;
}
