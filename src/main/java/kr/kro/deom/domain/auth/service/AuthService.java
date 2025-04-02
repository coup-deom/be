package kr.kro.deom.domain.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.kro.deom.common.exception.exceptions.UnauthorizedException;
import kr.kro.deom.common.exception.messages.UnauthorizedMessages;
import kr.kro.deom.common.security.jwt.JwtUtil;
import kr.kro.deom.domain.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final JwtUtil jwtUtil;

  // 여기서 access, refresh 토큰 발급하기

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
      throw new UnauthorizedException(UnauthorizedMessages.REFRESH_TOKEN_EXPIRED);
    }

    Long userId = jwtUtil.getUserId(refreshToken);
    Role role = Role.valueOf(jwtUtil.getRole(refreshToken));

    if (refreshToken != null && !refreshToken.equals(jwtUtil.getRefreshToken(userId))) {
      throw new UnauthorizedException(UnauthorizedMessages.INVALID_REFRESH_TOKEN);
    }

    String newAccessToken = jwtUtil.createAccessToken(userId, role);
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
