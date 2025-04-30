package kr.kro.deom.common.config;

import kr.kro.deom.common.security.handler.CustomAuthenticationEntryPoint;
import kr.kro.deom.common.security.handler.OAuth2SuccessHandler;
import kr.kro.deom.common.security.jwt.JwtAuthenticationFilter;
import kr.kro.deom.common.security.jwt.JwtUtil;
import kr.kro.deom.common.security.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 기본 보안 설정 OFF
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // 세션 사용 안 함 (JWT 기반)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(
                        exceptionHandler ->
                                exceptionHandler.authenticationEntryPoint(
                                        new CustomAuthenticationEntryPoint()))
                /*
                                   .accessDeniedHandler(new CustomAccessDeniedHandler()))

                */

                // 인가 설정
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())

                // JWT 필터 등록
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class)

                // OAuth2 설정
                .oauth2Login(
                        oauth ->
                                oauth.userInfoEndpoint(
                                                userInfoEndpoint ->
                                                        userInfoEndpoint.userService(
                                                                customOAuth2UserService))
                                        .successHandler(oAuth2SuccessHandler))
                .build();
    }
}
