package cn.lilicould.liliblog.util;

import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.config.properties.JwtProperties;
import cn.lilicould.liliblog.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // 重要：在这里通过Base64解码配置文件中的secret，并生成SecretKey对象
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 从JWT令牌中提取用户名
     *
     * @param token jwt令牌
     * @return 用户名
     * @author lilicould
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从JWT中提取用户
     * @param token jwt令牌
     * @return 用户信息
     */
    public User extractUser(String token) {
        return extractClaim(token, claims -> {
            Long userId = claims.get("userId", Long.class);
            String username = claims.get("userName", String.class);
            Integer role = claims.get("role", Integer.class);

            // status需要启用，否则会提示无权限操作
            Integer status = StatusConstant.ENABLED;

            return User.builder()
                    .id(userId)
                    .username(username)
                    .role(role)
                    .status(status)
                    .build();
        });
    }

    /**
     * 从JWT令牌中提取过期时间
     *
     * @param token jwt令牌
     * @return 过期时间
     * @author lilicould
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从JWT令牌中提取指定类型的声明
     *
     * @param token          jwt令牌
     * @param claimsResolver 声明解析函数
     * @return 解析后的声明值
     * @author lilicould
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析并验证JWT令牌，提取所有声明
     *
     * @param token jwt令牌
     * @return 声明对象
     * @author lilicould
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    /**
     * 判断JWT令牌是否已过期
     *
     * @param token jwt令牌
     * @return 是否过期
     * @author lilicould
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 生成访问令牌
     *
     * @param username 用户名
     * @return 访问令牌
     * @author lilicould
     */
    public String generateToken(String username, User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("userName", user.getUsername());
        claims.put("role", user.getRole());
        return createToken(claims, username, jwtProperties.getExpiration());
    }

    /**
     * 生成刷新令牌
     *
     * @param username 用户名
     * @return 刷新令牌
     * @author lilicould
     */
    public String generateRefreshToken(String username,User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("userName", user.getUsername());
        claims.put("role", user.getRole());
        return createToken(claims, username, jwtProperties.getRefreshExpiration());
    }

    /**
     * 构建JWT令牌
     *
     * @param claims     声明集合
     * @param subject   主题
     * @param expiration 过期时间（毫秒）
     * @return jwt令牌
     * @author lilicould
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 验证JWT令牌是否有效（用户名匹配且未过期）
     *
     * @param token    jwt令牌
     * @param username 用户名
     * @return 是否有效
     * @author lilicould
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * 检查JWT令牌是否有效（仅检查是否过期）
     *
     * @param token jwt令牌
     * @return 是否有效
     * @author lilicould
     */
    public Boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }


    /**
     * 获取令牌剩余有效时间（秒）
     *
     * @param token jwt令牌
     * @return 剩余有效时间（秒）
     * @author lilicould
     */
    public Long extractExpiresIn(String token) {
        Date expiration = extractExpiration(token);
        long remainingMs = expiration.getTime() - System.currentTimeMillis();

        // 如果已过期，返回 0
        if (remainingMs <= 0) {
            return 0L;
        }

        // 转换为秒并向上取整，确保剩余时间不为 0 时至少还有 1 秒
        return (long) Math.ceil(remainingMs / 1000.0);
    }
}