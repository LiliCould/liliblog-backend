package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.common.util.JwtUtil;
import cn.lilicould.liliblog.pojo.dto.request.LoginRequest;
import cn.lilicould.liliblog.pojo.dto.response.LoginVO;
import cn.lilicould.liliblog.pojo.dto.response.UserInfoVO;
import cn.lilicould.liliblog.pojo.entity.SecurityUser;
import cn.lilicould.liliblog.service.UserService;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口")
@ApiSupport(author = "lilicould")
public class AuthController {

    private final UserService userService;          // 你的用户服务
    private final PasswordEncoder passwordEncoder;  // BCryptPasswordEncoder
    private final JwtUtil jwtUtil;                  // JWT 工具类
    // todo 使用refresh服务类生成refresh token
    //private final RefreshTokenService refreshTokenService; // Redis 管理

    @PostMapping("/login")
    @Operation(summary = "登录接口", description = "通过账号密码登录")
    @ApiResponse(responseCode = "200",description = "响应成功，登录成功与否看响应状态码")
    public Result<LoginVO> login(@RequestBody @Valid LoginRequest request,
                                 HttpServletResponse response) {
        // 从数据库查询用户,这里查询不用判空，若空会自己抛出异常
        SecurityUser user = (SecurityUser) userService.loadUserByUsername(request.getUsername());

        // 密码不匹配 → 返回认证失败
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error(CodeEnum.ACCOUNT_OR_PASSWORD_ERROR);
        }

        // 检查账号状态
        if (!user.isEnabled()) {
            return Result.error(CodeEnum.ACCOUNT_DISABLED);
        }

        // 生成 Access Token 和 Refresh Token
        String accessToken = jwtUtil.generateToken(user.getUsername());
        long accessExpiresIn = jwtUtil.extractExpiration(accessToken).getTime() - new Date().getTime();

        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);           // HTTPS 环境
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(Integer.parseInt(accessExpiresIn + ""));
        response.addCookie(cookie);

        // todo 将 refreshToken 存储到 Redis，用于后续校验和吊销
        // refreshTokenService.saveRefreshToken(user.getId(), refreshToken, jwtUtil.getRefreshTokenTtl());

        // 构建用户视图对象
        UserInfoVO userInfo = UserInfoVO.from(user.toUser());

        // 返回登录响应
        LoginVO loginVO = LoginVO.builder()
                .accessToken(accessToken)
                .expiresIn(accessExpiresIn)
                .userInfo(userInfo)
                .build();

        return Result.success(loginVO);
    }
}
