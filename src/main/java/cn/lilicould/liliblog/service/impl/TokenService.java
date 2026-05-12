package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.common.cache.RedisHelper;
import cn.lilicould.liliblog.common.constant.RedisPrefixConstant;
import cn.lilicould.liliblog.common.util.JwtUtil;
import cn.lilicould.liliblog.config.properties.HttpOnlyCookiesProperties;
import cn.lilicould.liliblog.pojo.dto.response.LoginVO;
import cn.lilicould.liliblog.pojo.dto.response.UserInfo;
import cn.lilicould.liliblog.pojo.entity.User;
import cn.lilicould.liliblog.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtUtil jwtUtil;
    private final RedisHelper redisHelper;
    private final UserService userService;
    private final HttpOnlyCookiesProperties httpOnlyCookiesProperties;

    public LoginVO createLoginResponse(User user, HttpServletResponse response) {
        // 生成 Token
        String accessToken = jwtUtil.generateToken(user.getUsername());
        long accessExpiresIn = jwtUtil.extractExpiresIn(accessToken);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // 设置 Cookie
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(httpOnlyCookiesProperties.isSsl());
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(Integer.parseInt(accessExpiresIn + ""));
        response.addCookie(cookie);

        // 存储 Redis
        redisHelper.set(RedisPrefixConstant.AUTH_REFRESH_TOKEN + user.getId(),
                refreshToken, jwtUtil.extractExpiresIn(refreshToken));

        // 更新登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);

        // 返回响应
        return LoginVO.builder()
                .accessToken(accessToken)
                .expiresIn(accessExpiresIn)
                .userInfo(UserInfo.from(user))
                .build();
    }
}
