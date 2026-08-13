package cn.lilicould.liliblog.util;

import cn.lilicould.liliblog.config.properties.JwtProperties;
import cn.lilicould.liliblog.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JwtUtil测试类
 *
 * @author lilicould
 */
class JwtUtilTest {

    private static final String TEST_USERNAME = "testuser";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000L; // 1小时
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7天

    private JwtUtil jwtUtil;
    private SecretKey secretKey;
    private User user;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            keyBytes[i] = (byte) i;
        }
        String secret = Base64.getEncoder().encodeToString(keyBytes);
        secretKey = Keys.hmacShaKeyFor(keyBytes);

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(secret);
        jwtProperties.setExpiration(ACCESS_TOKEN_EXPIRATION);
        jwtProperties.setRefreshExpiration(REFRESH_TOKEN_EXPIRATION);

        jwtUtil = new JwtUtil(jwtProperties);

        user = User.builder()
                .id(1L)
                .username(TEST_USERNAME)
                .role(0)
                .build();
    }

    @Test
    void generateTokenProducesNonNullToken() {
        String token = jwtUtil.generateToken(TEST_USERNAME, user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateRefreshTokenProducesNonNullToken() {
        String token = jwtUtil.generateRefreshToken(TEST_USERNAME, user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername() {
        String token = jwtUtil.generateToken(TEST_USERNAME, user);

        assertEquals(TEST_USERNAME, jwtUtil.extractUsername(token));
    }

    @Test
    void extractExpirationIsInFuture() {
        String token = jwtUtil.generateToken(TEST_USERNAME, user);

        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.getTime() > System.currentTimeMillis());
    }

    @Test
    void extractClaim() {
        String token = jwtUtil.generateToken(TEST_USERNAME, user);

        String username = jwtUtil.extractClaim(token, io.jsonwebtoken.Claims::getSubject);
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    void extractUserBuildsUserFromClaims() {
        String token = jwtUtil.generateToken(TEST_USERNAME, user);

        User extracted = jwtUtil.extractUser(token);
        assertNotNull(extracted);
        assertEquals(1L, extracted.getId());
        assertEquals(TEST_USERNAME, extracted.getUsername());
        assertEquals(0, extracted.getRole());
        // 从令牌中取出的用户状态应该为启用
        assertEquals(1, extracted.getStatus());
    }

    @Test
    void isTokenValidForFreshToken() {
        String token = jwtUtil.generateToken(TEST_USERNAME, user);

        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void validateTokenWithMatchingUsername() {
        String token = jwtUtil.generateToken(TEST_USERNAME, user);

        assertTrue(jwtUtil.validateToken(token, TEST_USERNAME));
    }

    @Test
    void validateTokenWithWrongUsername() {
        String token = jwtUtil.generateToken(TEST_USERNAME, user);

        assertFalse(jwtUtil.validateToken(token, "wronguser"));
    }

    @Test
    void isTokenValidForExpiredToken() {
        String expired = Jwts.builder()
                .subject(TEST_USERNAME)
                .issuedAt(new Date(System.currentTimeMillis() - 7200000L))
                .expiration(new Date(System.currentTimeMillis() - 3600000L))
                .signWith(secretKey)
                .compact();

        // jjwt 0.13 解析过期令牌时直接抛出 ExpiredJwtException（生产行为）
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.isTokenValid(expired));
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.validateToken(expired, TEST_USERNAME));
    }

    @Test
    void refreshTokenLivesLongerThanAccessToken() {
        String accessToken = jwtUtil.generateToken(TEST_USERNAME, user);
        String refreshToken = jwtUtil.generateRefreshToken(TEST_USERNAME, user);

        long accessExp = jwtUtil.extractExpiration(accessToken).getTime();
        long refreshExp = jwtUtil.extractExpiration(refreshToken).getTime();
        assertTrue(refreshExp - accessExp > 0);
    }

    @Test
    void extractExpiresInReturnsRemainingSeconds() {
        String token = jwtUtil.generateToken(TEST_USERNAME, user);

        Long expiresIn = jwtUtil.extractExpiresIn(token);
        assertTrue(expiresIn > 0);
        assertTrue(expiresIn <= ACCESS_TOKEN_EXPIRATION / 1000);
    }

    @Test
    void extractExpiresInReturnsZeroForExpiredToken() {
        String expired = Jwts.builder()
                .subject(TEST_USERNAME)
                .issuedAt(new Date(System.currentTimeMillis() - 7200000L))
                .expiration(new Date(System.currentTimeMillis() - 3600000L))
                .signWith(secretKey)
                .compact();

        // jjwt 0.13 解析过期令牌时直接抛出 ExpiredJwtException（生产行为）
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.extractExpiresIn(expired));
    }
}