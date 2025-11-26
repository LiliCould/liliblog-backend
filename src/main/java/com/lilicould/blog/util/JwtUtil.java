package com.lilicould.blog.util;

import com.lilicould.blog.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import java.util.Date;

/**
 * JWT工具类
 * 用于生成和解析JWT
 * @author LiliCould
 */

@Component
public class JwtUtil {
    private final JwtConfig jwtConfig;

    private final SecretKey secretKey;

    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        // 生成安全的密钥
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 生成JWT
     * @param username 用户名
     * @param role 角色
     * @return 生成的JWT字符串
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析JWT
     * @param token JWT字符串
     * @return 解析后的Claims对象
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取用户名
     * @param token JWT字符串
     * @return 解析得到的用户名
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * 获取角色
     * @param token JWT字符串
     * @return 解析得到的角色
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * 获取过期时间
     * @param token JWT字符串
     * @return 解析得到的过期时间
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * 判断JWT是否过期
     * @param token JWT字符串
     * @return 布尔值，true表示已过期，false表示未过期
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 验证JWT，验证用户名和过期时间
     * @param token JWT字符串
     * @param username 用户名
     * @return 布尔值，true表示验证通过，false表示验证失败，可能是用户名不匹配或者已过期
     */
    public boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证JWT，验证过期时间
     * @param token JWT字符串
     * @return 布尔值，true表示验证通过，false表示验证失败，可能是已过期
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
