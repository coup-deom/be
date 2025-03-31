package kr.kro.deom.domain.user.service;

import kr.kro.deom.common.security.oauth.OAuth2UserInfo;
import kr.kro.deom.domain.user.entity.Role;
import kr.kro.deom.domain.user.entity.User;
import kr.kro.deom.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

  private final UserRepository userRepository;

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
}
