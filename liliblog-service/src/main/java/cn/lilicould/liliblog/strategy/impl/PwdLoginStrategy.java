package cn.lilicould.liliblog.strategy.impl;

import cn.lilicould.liliblog.constant.LoginStrategyConstant;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.domain.security.SecurityUser;
import cn.lilicould.liliblog.request.LoginRequest;
import cn.lilicould.liliblog.request.PwdLoginRequest;
import cn.lilicould.liliblog.response.LoginVO;
import cn.lilicould.liliblog.service.UserService;
import cn.lilicould.liliblog.service.impl.TokenService;
import cn.lilicould.liliblog.strategy.LoginStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class PwdLoginStrategy implements LoginStrategy {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

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

        // 生成token返回
        return tokenService.createLoginResponse(user.toUser(),response);
    }

    @Override
    public String getType() {
        return LoginStrategyConstant.PWD;
    }
}
