package com.lilicould.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT配置类,用于配置JWT的参数
 */
@Component
public class JwtConfig {

    /**
     * 密钥
     */
    @Value("${jwt.secret:jwt.secret=mynameisliliandiamabackenddeveloper}")
    private String secret;

    /**
     * 过期时间
     */
    @Value("${jwt.expire:86400000}") // 24小时
    private Long expiration;

    public String getSecret() {
        return secret;
    }

    public Long getExpiration() {
        return expiration;
    }
}
