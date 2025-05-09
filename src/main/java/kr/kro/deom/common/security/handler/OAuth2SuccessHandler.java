package kr.kro.deom.common.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.kro.deom.common.security.jwt.JwtUtil;
import kr.kro.deom.common.security.oauth.CustomOAuth2User;
import kr.kro.deom.domain.store.service.StoreService;
import kr.kro.deom.domain.user.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final StoreService storeService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        if (oAuth2User.getRole() == Role.PENDING) {
            String targetUrl = "http://localhost:5173/user/role?userId=" + oAuth2User.getId();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }

        boolean storeApproved = false;
        if (oAuth2User.getRole() == Role.OWNER) {
            storeApproved = storeService.isStoreApproved(oAuth2User.getId());
        }

        String accessToken =
                jwtUtil.createAccessToken(
                        oAuth2User.getId(),
                        oAuth2User.getRole(),
                        oAuth2User.getName(),
                        storeApproved);
        String refreshToken = jwtUtil.createRefreshToken(oAuth2User.getId(), oAuth2User.getRole());

        response.addCookie(jwtUtil.createRefreshTokenCookie(refreshToken));

        String targetUrl = "http://localhost:5173/signin/callback?accessToken=" + accessToken;

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
