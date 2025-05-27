package kr.kro.deom.common.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.kro.deom.common.security.jwt.JwtUtil;
import kr.kro.deom.common.security.oauth.CustomOAuth2User;
import kr.kro.deom.domain.store.service.StoreService;
import kr.kro.deom.domain.user.entity.Role;
import kr.kro.deom.domain.user.entity.User;
import kr.kro.deom.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final StoreService storeService;
    private final UserService userService;
    private static final String REDIRECT_URL = "https://staging.deom.kro.kr/signin/callback";

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Long userId = oAuth2User.getId();
        Role role = oAuth2User.getRole();

        User user = userService.getUser(userId);
        String nickname = user.getNickname();

        boolean storeApproved = false;
        Long storeId = null;

        if (role == Role.OWNER) {
            storeApproved = storeService.isStoreApproved(userId);
            storeId = storeService.getStoreIdByOwnerId(userId);
        }

        String accessToken = jwtUtil.createAccessToken(userId);
        String idToken = jwtUtil.createIdToken(userId, role, nickname, storeApproved, storeId);
        String refreshToken = jwtUtil.createRefreshToken(userId);

        response.addCookie(jwtUtil.createRefreshTokenCookie(refreshToken));

        String targetUrl =
                UriComponentsBuilder.fromUriString(REDIRECT_URL)
                        .queryParam("accessToken", accessToken)
                        .queryParam("idToken", idToken)
                        .build()
                        .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
