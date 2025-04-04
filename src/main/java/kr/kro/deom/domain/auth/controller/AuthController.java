package kr.kro.deom.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.kro.deom.common.exception.response.GlobalResponse;
import kr.kro.deom.common.security.oauth.CustomOAuth2User;
import kr.kro.deom.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/reissue")
  public ResponseEntity<GlobalResponse<String>> reissueToken(
      HttpServletRequest request, HttpServletResponse response) {
    String accessToken = authService.refreshAccessToken(request, response);
    return ResponseEntity.ok(GlobalResponse.success(accessToken));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(
      @AuthenticationPrincipal CustomOAuth2User user, HttpServletResponse response) {
    authService.logout(user.getUserId(), response);
    return ResponseEntity.ok(GlobalResponse.success("로그아웃 성공"));
  }
}
