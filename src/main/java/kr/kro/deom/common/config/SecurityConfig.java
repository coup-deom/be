package kr.kro.deom.common.config;

import java.util.List;
import kr.kro.deom.common.security.handler.CustomAuthenticationEntryPoint;
import kr.kro.deom.common.security.handler.OAuth2SuccessHandler;
import kr.kro.deom.common.security.jwt.JwtAuthenticationFilter;
import kr.kro.deom.common.security.jwt.JwtUtil;
import kr.kro.deom.common.security.oauth.CustomOAuth2UserService;
import kr.kro.deom.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserService userService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 기본 보안 설정 OFF
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // cors 설정
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

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
                        new JwtAuthenticationFilter(jwtUtil, userService),
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

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:5173"));
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowCredentials(true);
//        config.setAllowedHeaders(List.of("*"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
}
