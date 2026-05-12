package cn.lilicould.liliblog.strategy.impl;

import cn.lilicould.liliblog.common.cache.RedisHelper;
import cn.lilicould.liliblog.common.constant.LoginStrategyConstant;
import cn.lilicould.liliblog.common.constant.RedisPrefixConstant;
import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.common.util.JwtUtil;
import cn.lilicould.liliblog.config.properties.HttpOnlyCookiesProperties;
import cn.lilicould.liliblog.pojo.dto.request.LoginRequest;
import cn.lilicould.liliblog.pojo.dto.request.PwdLoginRequest;
import cn.lilicould.liliblog.pojo.dto.response.LoginVO;
import cn.lilicould.liliblog.pojo.dto.response.UserInfo;
import cn.lilicould.liliblog.pojo.entity.SecurityUser;
import cn.lilicould.liliblog.pojo.entity.User;
import cn.lilicould.liliblog.service.UserService;
import cn.lilicould.liliblog.strategy.LoginStrategy;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@Slf4j
public class PwdLoginStrategy implements LoginStrategy {
    private final RedisHelper redisHelper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final HttpOnlyCookiesProperties httpOnlyCookiesProperties;

    public PwdLoginStrategy(RedisHelper redisHelper, UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, HttpOnlyCookiesProperties httpOnlyCookiesProperties) {
        this.redisHelper = redisHelper;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.httpOnlyCookiesProperties = httpOnlyCookiesProperties;
    }

    @Override
    public LoginVO login(LoginRequest request, HttpServletResponse response) {

        PwdLoginRequest req = (PwdLoginRequest) request;

        // 从数据库查询用户,这里查询不用判空，若空会自己抛出异常
        SecurityUser user = (SecurityUser) userService.loadUserByUsername(req.getUsername());

        // 距离上次成功登录时间过短
        if (user.getLastLoginTime() != null) {
            long millis = Duration.between(user.getLastLoginTime(), LocalDateTime.now()).toMillis();
            if (Math.abs(millis) < 5000) { // 这里用abs是为了防止一些时区问题，不然出现负数永远都无法登录了
                log.error("距离上次成功登录时间过短{}", millis);
                throw new BusinessException(CodeEnum.LOGIN_TOO_FREQUENT);
            }
        }

        // 密码不匹配 → 返回认证失败
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(CodeEnum.ACCOUNT_OR_PASSWORD_ERROR);
        }

        // 检查账号状态
        if (!user.isEnabled()) {
            throw  new BusinessException(CodeEnum.ACCOUNT_DISABLED);
        }

        // 生成 Access Token 和 Refresh Token
        String accessToken = jwtUtil.generateToken(user.getUsername());
        long accessExpiresIn = jwtUtil.extractExpiresIn(accessToken);

        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // 用http-only-cookie存储刷新令牌
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(httpOnlyCookiesProperties.isSsl());           // HTTPS 环境
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(Integer.parseInt(accessExpiresIn + ""));
        response.addCookie(cookie);

        // 把刷星令牌存储到redis
        redisHelper.set(RedisPrefixConstant.AUTH_REFRESH_TOKEN + user.getId(),refreshToken,jwtUtil.extractExpiresIn(refreshToken));

        // 封装用户信息
        UserInfo userInfo = UserInfo.from(user.toUser());

        // 更新last_login_time
        User newUser = user.toUser();
        newUser.setLastLoginTime(LocalDateTime.now());
        userService.updateById(newUser);

        // 返回登录响应
        return LoginVO.builder()
                .accessToken(accessToken)
                .expiresIn(accessExpiresIn)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public String getType() {
        return LoginStrategyConstant.PWD;
    }
}
