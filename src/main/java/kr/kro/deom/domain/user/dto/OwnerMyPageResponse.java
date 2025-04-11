package kr.kro.deom.domain.user.dto;

import kr.kro.deom.common.security.oauth.OAuth2Provider;

public record OwnerMyPageResponse(String nickname, OAuth2Provider provider) {}
