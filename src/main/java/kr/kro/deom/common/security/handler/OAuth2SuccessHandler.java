package kr.kro.deom.common.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.kro.deom.common.security.jwt.JwtUtil;
import kr.kro.deom.common.security.oauth.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtUtil jwtUtil;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

    // role이 일단 pending 상태니까 여기서 역할 설정하는 곳으로 넘어가기

    String accessToken = jwtUtil.createAccessToken(oAuth2User.getId(), oAuth2User.getRole());
    String refreshToken = jwtUtil.createRefreshToken(oAuth2User.getId());

    // refreshToken cookie에 저장

    String redirectUrl = "https://frontend/oauth?accessToken=" + accessToken;

    response.sendRedirect(redirectUrl);
  }
}
