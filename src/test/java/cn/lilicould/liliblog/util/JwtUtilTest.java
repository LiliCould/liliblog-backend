package cn.lilicould.liliblog.util;

import cn.lilicould.liliblog.common.util.JwtUtil;
import cn.lilicould.liliblog.config.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil测试类
 *
 * @author lilicould
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_USERNAME = "testuser";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000L; // 1小时
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7天

    /**
     * 测试前初始化
     * 生成一个256位的密钥用于测试
     */
    @BeforeEach
    void setUp() {
        // 生成一个256位的密钥用于测试
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            keyBytes[i] = (byte) i;
        }
        String secret = Base64.getEncoder().encodeToString(keyBytes);

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(secret);
        jwtProperties.setExpiration(ACCESS_TOKEN_EXPIRATION);
        jwtProperties.setRefreshExpiration(REFRESH_TOKEN_EXPIRATION);

        jwtUtil = new JwtUtil(jwtProperties);
    }

    /**
     * 测试生成访问令牌
     * 验证generateToken方法能够生成非空的JWT令牌
     */
    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken(TEST_USERNAME);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    /**
     * 测试生成刷新令牌
     * 验证generateRefreshToken方法能够生成非空的JWT刷新令牌
     */
    @Test
    void testGenerateRefreshToken() {
        String token = jwtUtil.generateRefreshToken(TEST_USERNAME);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    /**
     * 测试提取用户名
     * 验证extractUsername方法能够正确从令牌中提取用户名
     */
    @Test
    void testExtractUsername() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        String username = jwtUtil.extractUsername(token);

        assertEquals(TEST_USERNAME, username);
    }

    /**
     * 测试提取过期时间
     * 验证extractExpiration方法能够正确提取令牌的过期时间
     */
    @Test
    void testExtractExpiration() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        var expiration = jwtUtil.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.getTime() > System.currentTimeMillis());
    }

    /**
     * 测试提取声明
     * 验证extractClaim方法能够正确提取指定类型的声明
     */
    @Test
    void testExtractClaim() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        String username = jwtUtil.extractClaim(token, claims -> claims.getSubject());

        assertEquals(TEST_USERNAME, username);
    }

    /**
     * 测试提取所有声明
     * 验证extractClaim方法能够提取完整的Claims对象
     */
    @Test
    void testExtractAllClaims() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        var claims = jwtUtil.extractClaim(token, claim -> claim);

        assertNotNull(claims);
        assertEquals(TEST_USERNAME, claims.getSubject());
    }

    /**
     * 测试令牌未过期
     * 验证isTokenValid方法对于新生成的令牌返回true（未过期）
     */
    @Test
    void testIsTokenExpired_NotExpired() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        Boolean isValid = jwtUtil.isTokenValid(token);

        assertTrue(isValid);
    }

    /**
     * 测试验证有效令牌
     * 验证validateToken方法对于正确的用户名和未过期的令牌返回true
     */
    @Test
    void testValidateToken_Valid() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        Boolean isValid = jwtUtil.validateToken(token, TEST_USERNAME);

        assertTrue(isValid);
    }

    /**
     * 测试用户名不匹配
     * 验证validateToken方法对于用户名不匹配的情况返回false
     */
    @Test
    void testValidateToken_WrongUsername() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        Boolean isValid = jwtUtil.validateToken(token, "wronguser");

        assertFalse(isValid);
    }

    /**
     * 测试检查令牌有效性
     * 验证isTokenValid方法能够正确判断令牌是否有效
     */
    @Test
    void testIsTokenValid_Valid() {
        String token = jwtUtil.generateToken(TEST_USERNAME);
        Boolean isValid = jwtUtil.isTokenValid(token);

        assertTrue(isValid);
    }

    /**
     * 测试令牌包含正确的声明
     * 验证生成的访问令牌和刷新令牌都包含正确的声明，且刷新令牌过期时间更长
     */
    @Test
    void testTokenContainsExpectedClaims() {
        String accessToken = jwtUtil.generateToken(TEST_USERNAME);
        String refreshToken = jwtUtil.generateRefreshToken(TEST_USERNAME);

        // 验证访问令牌
        assertNotNull(jwtUtil.extractUsername(accessToken));
        assertNotNull(jwtUtil.extractExpiration(accessToken));

        // 验证刷新令牌
        assertNotNull(jwtUtil.extractUsername(refreshToken));
        assertNotNull(jwtUtil.extractExpiration(refreshToken));

        // 验证刷新令牌的过期时间比访问令牌长
        long accessExp = jwtUtil.extractExpiration(accessToken).getTime();
        long refreshExp = jwtUtil.extractExpiration(refreshToken).getTime();
        assertTrue(refreshExp - accessExp > 0);
    }
}