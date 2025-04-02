package kr.kro.deom.domain.user.service;

import kr.kro.deom.common.exception.exceptions.NotFoundException;
import kr.kro.deom.common.exception.messages.NotFoundMessages;
import kr.kro.deom.common.security.oauth.OAuth2UserInfo;
import kr.kro.deom.domain.user.dto.UserResponse;
import kr.kro.deom.domain.user.entity.Role;
import kr.kro.deom.domain.user.entity.User;
import kr.kro.deom.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

  private final UserRepository userRepository;

  public User getUser(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new NotFoundException(NotFoundMessages.USER));
  }

  public UserResponse getUserInfo(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException(NotFoundMessages.USER));

    return UserResponse.from(user);
  }

  public User findOrCreateUser(OAuth2UserInfo info) {
    return userRepository
        .findUserBySocialId(info.getProviderId())
        .orElseGet(
            () -> {
              User newUser =
                  User.builder()
                      .socialId(info.getProviderId())
                      .provider(info.getProvider())
                      .email(info.getEmail())
                      .nickname(info.getName())
                      .role(Role.PENDING)
                      .build();
              return userRepository.save(newUser);
            });
  }

  public void deleteUser(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException(NotFoundMessages.USER);
    }
    userRepository.deleteById(userId);
  }

  public User setUserRole(Long userId, Role role) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException(NotFoundMessages.USER));
    user.updateRole(role);
    return userRepository.save(user);
  }
}
