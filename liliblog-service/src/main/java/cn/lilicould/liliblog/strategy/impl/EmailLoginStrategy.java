package cn.lilicould.liliblog.strategy.impl;

import cn.lilicould.liliblog.cache.RedisHelper;
import cn.lilicould.liliblog.constant.LoginStrategyConstant;
import cn.lilicould.liliblog.constant.RedisPrefixConstant;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.domain.security.SecurityUser;
import cn.lilicould.liliblog.request.EmailLoginRequest;
import cn.lilicould.liliblog.request.LoginRequest;
import cn.lilicould.liliblog.response.LoginVO;
import cn.lilicould.liliblog.entity.User;
import cn.lilicould.liliblog.service.UserService;
import cn.lilicould.liliblog.service.impl.TokenService;
import cn.lilicould.liliblog.strategy.LoginStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@Slf4j
public class EmailLoginStrategy implements LoginStrategy {

    private final UserService userService;
    private final TokenService tokenService;
    private final RedisHelper redisHelper;

    public EmailLoginStrategy(UserService userService, TokenService tokenService, RedisHelper redisHelper) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.redisHelper = redisHelper;
    }

    @Override
    public LoginVO login(LoginRequest request, HttpServletResponse response) {
        // 获取参数
        EmailLoginRequest req = (EmailLoginRequest) request;

        // 从redis中获取验证码比对
        String code = req.getCode();
        String redisCode = redisHelper.get(RedisPrefixConstant.AUTH_EMAIL_CODE + req.getEmail(), String.class);
        if (!code.equals(redisCode)) {
            // 验证码错误
            throw new BusinessException(CodeEnum.CODE_ERROR);
        }

        // 从数据库查询用户,这里查询不用判空，若空会自己抛出异常
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail()));
        if (user == null) {
            throw new BusinessException(CodeEnum.USER_NOT_FOUND);
        }
        // 检查账号是否被禁用
        SecurityUser securityUser = new SecurityUser(user);
        if (!securityUser.isEnabled()) {
            throw new BusinessException(CodeEnum.ACCOUNT_DISABLED);
        }

        // 距离上次成功登录时间过短
        if (user.getLastLoginTime() != null) {
            long millis = Duration.between(user.getLastLoginTime(), LocalDateTime.now()).toMillis();
            if (Math.abs(millis) < 5000) { // 这里用abs是为了防止一些时区问题，不然出现负数永远都无法登录了
                log.error("距离上次成功登录时间过短{}", millis);
                throw new BusinessException(CodeEnum.LOGIN_TOO_FREQUENT);
            }
        }

        // 清除验证码
        redisHelper.delete(RedisPrefixConstant.AUTH_EMAIL_CODE + req.getEmail());

        // 生成token返回
        return tokenService.createLoginResponse(user,response);


    }

    @Override
    public String getType() {
        return LoginStrategyConstant.EMAIL;
    }
}
