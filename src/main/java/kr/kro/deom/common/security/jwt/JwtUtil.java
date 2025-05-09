package kr.kro.deom.common.security.jwt;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import kr.kro.deom.domain.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
@Repository
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    private final RedisTemplate<String, String> redisTemplate;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long userId, Role role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    public String createIdToken(
            Long userId, Role role, String nickname, Boolean storeApproved, Long storeId) {
        JwtBuilder builder =
                Jwts.builder()
                        .claim("userId", userId)
                        .claim("role", role.name())
                        .claim("nickname", nickname)
                        .issuedAt(new Date())
                        .expiration(
                                new Date(
                                        System.currentTimeMillis()
                                                + accessTokenExpiration)) // 짧게 유지
                        .signWith(key);

        if (role == Role.OWNER) {
            builder.claim("storeApproved", storeApproved);
            builder.claim("storeId", storeId);
        }

        return builder.compact();
    }

    public String createRefreshToken(Long userId, Role role) {
        String refreshToken =
                Jwts.builder()
                        .subject(String.valueOf(userId))
                        .claim("role", role.name())
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                        .signWith(key)
                        .compact();

        saveRefreshToken(userId, refreshToken);

        return refreshToken;
    }

    public Long getUserId(String token) {
        String subject =
                Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject();

        return Long.parseLong(subject);
    }

    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public String getNickname(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("nickname", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);

        return cookie;
    }

    public void saveRefreshToken(Long userId, String refreshToken) {
        redisTemplate
                .opsForValue()
                .set(
                        "refreshToken:" + userId,
                        refreshToken,
                        refreshTokenExpiration,
                        TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get("refreshToken:" + userId.toString());
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete("refreshToken:" + userId.toString());
    }
}
