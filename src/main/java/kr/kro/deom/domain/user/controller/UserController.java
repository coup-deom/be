package kr.kro.deom.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "유저 API", description = "유저 조회, 탈퇴, 역할 변경")
public class UserController {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "유저 정보 조회", description = "로그인 한 유저 정보를 조회한다.")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomOAuth2User user) {
        UserResponse response = userService.getUserInfo(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, response));
    }

    @GetMapping("/withdrawal")
    @Operation(summary = "회원 탈퇴", description = "유저가 회원을 탈퇴한다.")
    public ResponseEntity<ApiResponse<String>> deleteMyAccount(
            @AuthenticationPrincipal CustomOAuth2User user) {
        userService.deleteUser(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, "회원 탈퇴 성공"));
    }

    @PostMapping("/role")
    @Operation(summary = "역할 변경", description = "유저가 자신의 역할을 설정한다.")
    public ResponseEntity<ApiResponse<String>> setRole(
            @RequestBody RoleRequest request, HttpServletResponse response) {
        User user = userService.setUserRole(request.getUserId(), request.getRole());

        String newAccessToken = jwtUtil.createAccessToken(user.getId(), user.getRole());
        String newRefreshToken = jwtUtil.createRefreshToken(user.getId(), user.getRole());

        response.addCookie(jwtUtil.createRefreshTokenCookie(newRefreshToken));

        return ResponseEntity.ok(ApiResponse.success(CommonSuccessCode.OK, newAccessToken));
    }
}
