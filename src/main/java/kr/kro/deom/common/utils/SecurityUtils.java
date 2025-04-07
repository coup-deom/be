package kr.kro.deom.common.utils;

import kr.kro.deom.common.security.oauth.CustomOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * 현재 인증된 사용자의 ID를 반환
     *
     * @return 로그인한 사용자 ID
     * @throws SecurityException 인증되지 않은 사용자인 경우
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new SecurityException("인증된 사용자가 아닙니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getUserId();
        }
        throw new SecurityException("지원되지 않는 인증 타입입니다.");
    }
}
