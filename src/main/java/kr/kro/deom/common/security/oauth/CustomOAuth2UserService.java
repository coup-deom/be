package kr.kro.deom.common.security.oauth;

import kr.kro.deom.domain.user.entity.User;
import kr.kro.deom.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserService userService;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    OAuth2User oAuth2User = super.loadUser(userRequest);

    String provider = userRequest.getClientRegistration().getRegistrationId();

    OAuth2UserInfo oAuth2UserInfo;
    if (provider.equals("kakao")) {
      oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
    } else if (provider.equals("google")) {
      oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
    } else {
      return null;
    }

    User user = userService.findOrCreateUser(oAuth2UserInfo);

    return new CustomOAuth2User(user.getId(), user.getRole(), oAuth2User.getAttributes());
  }
}
