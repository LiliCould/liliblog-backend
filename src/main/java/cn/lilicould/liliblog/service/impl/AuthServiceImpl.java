package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.common.cache.RedisHelper;
import cn.lilicould.liliblog.common.constant.RedisPrefixConstant;
import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.enums.RoleType;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.common.util.JwtUtil;
import cn.lilicould.liliblog.config.properties.HttpOnlyCookiesProperties;
import cn.lilicould.liliblog.pojo.dto.request.LoginRequest;
import cn.lilicould.liliblog.pojo.dto.request.RegisterRequest;
import cn.lilicould.liliblog.pojo.dto.response.LoginVO;
import cn.lilicould.liliblog.pojo.dto.response.UserInfoVO;
import cn.lilicould.liliblog.pojo.entity.SecurityUser;
import cn.lilicould.liliblog.pojo.entity.User;
import cn.lilicould.liliblog.service.AuthService;
import cn.lilicould.liliblog.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final HttpOnlyCookiesProperties httpOnlyCookiesProperties;
    private final UserService userService;
    private final RedisHelper redisHelper;

    @Override
    public LoginVO login(LoginRequest request, HttpServletResponse response) {
        // 从数据库查询用户,这里查询不用判空，若空会自己抛出异常
        SecurityUser user = (SecurityUser) userService.loadUserByUsername(request.getUsername());

        // 距离上次成功登录时间过短
        if (user.getLastLoginTime() != null) {
            long millis = Duration.between(user.getLastLoginTime(), LocalDateTime.now()).toMillis();
            if (Math.abs(millis) < 5000) { // 这里用abs是为了防止一些时区问题，不然出现负数永远都无法登录了
                log.error("距离上次成功登录时间过短{}", millis);
                throw new BusinessException(CodeEnum.LOGIN_TOO_FREQUENT);
            }
        }

        // 密码不匹配 → 返回认证失败
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
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

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(httpOnlyCookiesProperties.isSsl());           // HTTPS 环境
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(Integer.parseInt(accessExpiresIn + ""));
        response.addCookie(cookie);

        // refreshTokenService.saveRefreshToken(user.getId(), refreshToken, jwtUtil.getRefreshTokenTtl());
        redisHelper.set(RedisPrefixConstant.AUTH_REFRESH_TOKEN + user.getId(),refreshToken,jwtUtil.extractExpiresIn(refreshToken));

        // 封装用户信息
        UserInfoVO userInfo = UserInfoVO.from(user.toUser());

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

    /**
     * 注册服务接口
     * @param request 注册参数
     */
    @Override
    public void register(RegisterRequest request) {
        // 密码校验
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(CodeEnum.PASSWORD_MISMATCH);
        }

        // 判重
        if (userService.exists(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()))) {
            throw new BusinessException(CodeEnum.USERNAME_ALREADY_EXISTS);
        }
        if (userService.exists(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()))) {
            throw new BusinessException(CodeEnum.EMAIL_ALREADY_EXISTS);
        }

        User user = new User();

        BeanUtils.copyProperties(request, user);

        user.setPassword(passwordEncoder.encode(request.getPassword())); // 密码加密存储
        user.setRole(RoleType.USER.getCode()); // 注册设置身份为USER
        user.setStatus(StatusConstant.ENABLED);

        userService.save(user);
    }
}
