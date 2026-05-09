package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.common.util.JwtUtil;
import cn.lilicould.liliblog.config.properties.HttpOnlyCookiesProperties;
import cn.lilicould.liliblog.mapper.UserMapper;
import cn.lilicould.liliblog.pojo.dto.request.LoginRequest;
import cn.lilicould.liliblog.pojo.dto.response.LoginVO;
import cn.lilicould.liliblog.pojo.dto.response.UserInfoVO;
import cn.lilicould.liliblog.pojo.entity.SecurityUser;
import cn.lilicould.liliblog.pojo.entity.User;
import cn.lilicould.liliblog.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author Lili_Could
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:41
*/
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final HttpOnlyCookiesProperties httpOnlyCookiesProperties;

    @Override
    @NullUnmarked
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = getOne(queryWrapper);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new SecurityUser(user);
    }

    @Override
    public LoginVO login(LoginRequest request, HttpServletResponse response) {
        // 从数据库查询用户,这里查询不用判空，若空会自己抛出异常
        SecurityUser user = (SecurityUser) loadUserByUsername(request.getUsername());

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
        long accessExpiresIn = jwtUtil.extractExpiration(accessToken).getTime() - new Date().getTime();

        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(httpOnlyCookiesProperties.isSsl());           // HTTPS 环境
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(Integer.parseInt(accessExpiresIn + ""));
        response.addCookie(cookie);

        // todo 将 refreshToken 存储到 Redis，用于后续校验和吊销
        // refreshTokenService.saveRefreshToken(user.getId(), refreshToken, jwtUtil.getRefreshTokenTtl());

        // 返回用户信息
        UserInfoVO userInfo = UserInfoVO.from(user.toUser());

        // 返回登录响应
        return LoginVO.builder()
                .accessToken(accessToken)
                .expiresIn(accessExpiresIn)
                .userInfo(userInfo)
                .build();
    }
}




