package kr.kro.deom.domain.user.service;

import jakarta.servlet.http.HttpServletResponse;
import kr.kro.deom.common.security.jwt.JwtUtil;
import kr.kro.deom.common.security.oauth.OAuth2UserInfo;
import kr.kro.deom.domain.auth.dto.TokenResponse;
import kr.kro.deom.domain.user.dto.RoleRequest;
import kr.kro.deom.domain.user.dto.UserResponse;
import kr.kro.deom.domain.user.entity.Role;
import kr.kro.deom.domain.user.entity.User;
import kr.kro.deom.domain.user.exception.UserNotFoundException;
import kr.kro.deom.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        return UserResponse.from(user);
    }

    @Transactional
    public User findOrCreateUser(OAuth2UserInfo info) {
        return userRepository
                .findUserBySocialId(info.getProviderId())
                .map(
                        user -> {
                            if (user.isDeleted()) {
                                user.updateDeleted(false);
                            }
                            return user;
                        })
                .orElseGet(
                        () -> {
                            User newUser =
                                    User.builder()
                                            .socialId(info.getProviderId())
                                            .provider(info.getProvider())
                                            .email(info.getEmail())
                                            .nickname(info.getName())
                                            .role(Role.PENDING)
                                            .deleted(false)
                                            .build();
                            return userRepository.save(newUser);
                        });
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.updateDeleted(true);
    }

    @Transactional
    public TokenResponse setUserRole(RoleRequest roleRequest, HttpServletResponse response) {
        User user = userRepository.findById(roleRequest.getUserId()).orElseThrow(UserNotFoundException::new);
        user.updateRole(roleRequest.getRole());

        Long userId = user.getId();
        Role role = user.getRole();
        String nickname = user.getNickname();

        String newAccessToken =
                jwtUtil.createAccessToken(userId, role);
        String newIdToken = jwtUtil.createIdToken(userId, role, nickname, false, null);
        String newRefreshToken = jwtUtil.createRefreshToken(userId, role);

        response.addCookie(jwtUtil.createRefreshTokenCookie(newRefreshToken));

        return new TokenResponse(newAccessToken, newIdToken);
    }

    public void validateUserByUserId(Long userId) {
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }
}
