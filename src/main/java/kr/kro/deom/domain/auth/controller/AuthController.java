package kr.kro.deom.domain.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.common.security.oauth.CustomOAuth2User;
import kr.kro.deom.domain.auth.dto.TokenResponse;
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
@Tag(name = "Auth API", description = "토큰 재발급, 로그아웃")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissueToken(
            HttpServletRequest request, HttpServletResponse response) {
        TokenResponse tokens = authService.refreshAccessToken(request, response);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @AuthenticationPrincipal CustomOAuth2User user, HttpServletResponse response) {
        authService.logout(user.getUserId(), response);
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, "로그아웃 성공"));
    }
}
