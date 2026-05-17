package cn.lilicould.liliblog.config.security.handler;

import cn.lilicould.liliblog.common.cache.RedisHelper;
import cn.lilicould.liliblog.common.constant.RedisPrefixConstant;
import cn.lilicould.liliblog.common.util.JwtUtil;
import cn.lilicould.liliblog.domain.security.OAuth2SecurityUser;
import cn.lilicould.liliblog.service.impl.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class SuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final RedisHelper redisHelper;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("OAuth2 登录成功");

        // 获取自定义用户对象
        OAuth2SecurityUser oauth2User = (OAuth2SecurityUser) authentication.getPrincipal();

        // 2. 用你的 TokenService 生成 Token（方法名你自己改成实际的）
        // 示例：假设你的 TokenService 能返回 token 字符串
        String accessToken = null;
        String refreshToken = null;
        long expireIn = 0;
        if (oauth2User != null) {
            accessToken = jwtUtil.generateToken(oauth2User.getUsername());
            refreshToken = jwtUtil.generateRefreshToken(oauth2User.getUsername());
            expireIn = jwtUtil.extractExpiresIn(refreshToken);
            redisHelper.set(RedisPrefixConstant.AUTH_REFRESH_TOKEN + oauth2User.getUsername(),refreshToken);
        }

        // 重定向到前端回调页，URL 带上 token
        String redirectUrl = UriComponentsBuilder
                .fromUriString("http://localhost:3000/oauth2/redirect")
                .queryParam("access_token", accessToken)
                .queryParam("refresh_token", refreshToken)
                .queryParam("expires_in", expireIn)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}