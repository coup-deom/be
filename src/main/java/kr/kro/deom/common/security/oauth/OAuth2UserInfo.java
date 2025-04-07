package kr.kro.deom.common.security.oauth;

public interface OAuth2UserInfo {

    OAuth2Provider getProvider();

    String getProviderId();

    String getEmail();

    String getName();
}
