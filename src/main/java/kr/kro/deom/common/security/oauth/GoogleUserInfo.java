package kr.kro.deom.common.security.oauth;

import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo {

  private final Map<String, Object> attributes;

  public GoogleUserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public OAuth2Provider getProvider() {
    return OAuth2Provider.GOOGLE;
  }

  @Override
  public String getProviderId() {
    return attributes.get("sub").toString();
  }

  @Override
  public String getEmail() {
    return attributes.get("email").toString();
  }

  @Override
  public String getName() {
    return attributes.get("name").toString();
  }
}
