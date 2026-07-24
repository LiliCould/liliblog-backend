package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.cache.RedisHelper;
import cn.lilicould.liliblog.constant.RedisPrefixConstant;
import cn.lilicould.liliblog.util.JwtUtil;
import cn.lilicould.liliblog.config.properties.HttpOnlyCookiesProperties;
import cn.lilicould.liliblog.response.LoginVO;
import cn.lilicould.liliblog.response.UserInfo;
import cn.lilicould.liliblog.entity.User;
import cn.lilicould.liliblog.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
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
        String accessToken = jwtUtil.generateToken(user.getUsername(),user);
        long accessExpiresIn = jwtUtil.extractExpiresIn(accessToken); // 单位秒
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(),user);
        long refreshExpiresIn = jwtUtil.extractExpiresIn(refreshToken);

        // 设置 Cookie
        String refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(httpOnlyCookiesProperties.isSsl()) // 这个必须设为true, 否则前端无法获取到 Cookie,因为sameSite设为了 None
                .sameSite("None") // 设置 SameSite,由于前后端分离，需要设置 SameSite为 None
                .maxAge(refreshExpiresIn)
                .path("/auth")
                .domain(httpOnlyCookiesProperties.getDomain())
                .build()
                .toString();
        // 不使用 response.addCookie()，因为response.addHeader原生支持Cookie，且可以设置 SameSite
        response.addHeader("Set-Cookie", refreshCookie);


        // 存储 Redis
        redisHelper.set(RedisPrefixConstant.AUTH_REFRESH_TOKEN + user.getUsername(),
                refreshToken, jwtUtil.extractExpiresIn(refreshToken)*1000);

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
