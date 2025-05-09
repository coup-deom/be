package kr.kro.deom.domain.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.kro.deom.common.security.jwt.JwtUtil;
import kr.kro.deom.domain.auth.exception.InvalidRefreshTokenException;
import kr.kro.deom.domain.auth.exception.RefreshTokenExpiredException;
import kr.kro.deom.domain.store.service.StoreService;
import kr.kro.deom.domain.user.entity.Role;
import kr.kro.deom.domain.user.entity.User;
import kr.kro.deom.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final StoreService storeService;
    private final UserService userService;

    public String refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null && !jwtUtil.validateToken(refreshToken)) {
            throw new RefreshTokenExpiredException();
        }

        Long userId = jwtUtil.getUserId(refreshToken);

        User user = userService.getUser(userId);

        Role role = user.getRole();
        String nickname = user.getNickname();

        boolean storeApproved = false;
        if (role == Role.OWNER) {
            storeApproved = storeService.isStoreApproved(userId);
        }

        if (refreshToken != null && !refreshToken.equals(jwtUtil.getRefreshToken(userId))) {
            throw new InvalidRefreshTokenException();
        }

        String newAccessToken = jwtUtil.createAccessToken(userId, role, nickname, storeApproved);
        String newRefreshToken = jwtUtil.createRefreshToken(userId, role);

        response.addCookie(jwtUtil.createRefreshTokenCookie(newRefreshToken));

        return newAccessToken;
    }

    public void logout(Long userId, HttpServletResponse response) {

        jwtUtil.deleteRefreshToken(userId);

        Cookie expiredCookie = new Cookie("refreshToken", null);
        expiredCookie.setHttpOnly(true);
        expiredCookie.setPath("/");
        expiredCookie.setMaxAge(0);

        response.addCookie(expiredCookie);
    }
}
