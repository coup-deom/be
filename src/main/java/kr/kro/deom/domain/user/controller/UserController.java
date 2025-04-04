package kr.kro.deom.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import kr.kro.deom.common.response.ApiResponse;
import kr.kro.deom.common.response.CommonSuccessCode;
import kr.kro.deom.common.security.jwt.JwtUtil;
import kr.kro.deom.common.security.oauth.CustomOAuth2User;
import kr.kro.deom.domain.user.dto.RoleRequest;
import kr.kro.deom.domain.user.dto.UserResponse;
import kr.kro.deom.domain.user.entity.User;
import kr.kro.deom.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

  private final JwtUtil jwtUtil;
  private final UserService userService;

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
      @AuthenticationPrincipal CustomOAuth2User user) {
    UserResponse response = userService.getUserInfo(user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
  }

  @GetMapping("/withdrawal")
  public ResponseEntity<ApiResponse<String>> deleteMyAccount(
      @AuthenticationPrincipal CustomOAuth2User user) {
    userService.deleteUser(user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, "회원 탈퇴 성공"));
  }

  @PostMapping("/role")
  public ResponseEntity<ApiResponse<String>> setRole(
      @RequestBody RoleRequest request, HttpServletResponse response) {
    User user = userService.setUserRole(request.getUserId(), request.getRole());

    String newAccessToken = jwtUtil.createAccessToken(user.getId(), user.getRole());
    String newRefreshToken = jwtUtil.createRefreshToken(user.getId(), user.getRole());

    response.addCookie(jwtUtil.createRefreshTokenCookie(newRefreshToken));

    return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, "role 설정 완료"));
  }
}
