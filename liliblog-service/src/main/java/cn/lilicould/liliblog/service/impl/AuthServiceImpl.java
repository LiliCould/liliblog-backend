package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.cache.RedisHelper;
import cn.lilicould.liliblog.constant.RedisPrefixConstant;
import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.enums.RoleType;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.request.LoginRequest;
import cn.lilicould.liliblog.request.RegisterRequest;
import cn.lilicould.liliblog.response.LoginVO;
import cn.lilicould.liliblog.entity.User;
import cn.lilicould.liliblog.service.AuthService;
import cn.lilicould.liliblog.service.UserService;
import cn.lilicould.liliblog.strategy.LoginStrategy;
import cn.lilicould.liliblog.strategy.LoginStrategyFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.text.DecimalFormat;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final LoginStrategyFactory factory; // 登录策略工厂
    private final RedisHelper redisHelper;
    private final EmailTemplateService emailTemplateService;


    @Override
    public LoginVO login(LoginRequest request, HttpServletResponse response) {
        LoginStrategy strategy = factory.getStrategy(request.getLoginType()); // 根据请求类型获取登录策略
        return strategy.login(request,response);
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

    /**
     * 获取邮箱验证码
     * @param email 邮箱
     * @return 验证码
     * @author lilicould
     */
    @Override
    public void getEmailCode(String email) {

        // 检查redis中是否存在验证码
        if (redisHelper.exists(RedisPrefixConstant.AUTH_EMAIL_CODE + email)) {
            throw new BusinessException(CodeEnum.REPEAT_OPERATION);
        }

        // 使用secureRandom生成6位验证码
        int num = new SecureRandom().nextInt(1000000); // 0-999999
        DecimalFormat df = new DecimalFormat("000000"); // 限制为6位
        String code = df.format(num);

        // 存储到redis中,分钟有效
        redisHelper.set(RedisPrefixConstant.AUTH_EMAIL_CODE + email, code, 5 * 60 * 1000);
        // 发送邮箱
        emailTemplateService.sendVerificationCodeEmail(email, code);
    }
}
