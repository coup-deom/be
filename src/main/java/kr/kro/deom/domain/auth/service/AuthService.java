package kr.kro.deom.domain.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.kro.deom.common.security.jwt.JwtUtil;
import kr.kro.deom.domain.auth.dto.TokenResponse;
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

    public TokenResponse refreshAccessToken(
            HttpServletRequest request, HttpServletResponse response) {
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

        if (refreshToken != null && !refreshToken.equals(jwtUtil.getRefreshToken(userId))) {
            throw new InvalidRefreshTokenException();
        }

        User user = userService.getUser(userId);
        Role role = user.getRole();
        String nickname = user.getNickname();

        Long storeId = null;
        boolean storeApproved = false;

        if (role == Role.OWNER) {
            storeApproved = storeService.isStoreApproved(userId);
            storeId = storeService.getStoreIdByOwnerId(userId);
        }

        String newAccessToken = jwtUtil.createAccessToken(userId);
        String newIdToken = jwtUtil.createIdToken(userId, role, nickname, storeApproved, storeId);
        String newRefreshToken = jwtUtil.createRefreshToken(userId);

        response.addCookie(jwtUtil.createRefreshTokenCookie(newRefreshToken));

        return new TokenResponse(newAccessToken, newIdToken);
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
