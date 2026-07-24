package cn.lilicould.liliblog.controller.user;

import cn.lilicould.cache.RedisHelper;
import cn.lilicould.constant.LoginStrategyConstant;
import cn.lilicould.constant.RedisPrefixConstant;
import cn.lilicould.enums.CodeEnum;
import cn.lilicould.exception.BusinessException;
import cn.lilicould.result.Result;
import cn.lilicould.liliblog.util.JwtUtil;
import cn.lilicould.liliblog.domain.security.SecurityUser;
import cn.lilicould.request.EmailLoginRequest;
import cn.lilicould.request.PwdLoginRequest;
import cn.lilicould.request.RegisterRequest;
import cn.lilicould.response.LoginVO;
import cn.lilicould.response.UserInfo;
import cn.lilicould.entity.User;
import cn.lilicould.liliblog.service.AuthService;
import cn.lilicould.liliblog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口")
public class AuthController {

    private final AuthService authService;          // 你的用户服务
    private final RedisHelper redisHelper;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login/pwd")
    @Operation(summary = "用户名密码登录", description = "通过用户名和密码登录")
    @ApiResponse(responseCode = "200",description = "响应成功，登录成功与否看响应状态码")
    public Result<LoginVO> login(@RequestBody @Validated PwdLoginRequest request,
                                 HttpServletResponse response) {

        request.setLoginType(LoginStrategyConstant.PWD);
        LoginVO loginVO = authService.login(request,response); // 调用登录接口

        return Result.success(loginVO);
    }

    @PostMapping("/login/email")
    @Operation(summary = "邮箱登录",description = "通过邮箱和验证码登录")
    public Result<LoginVO> wechatLogin(@RequestBody @Validated EmailLoginRequest request,
                                       HttpServletResponse response) {
        request.setLoginType(LoginStrategyConstant.EMAIL);

        LoginVO loginVO = authService.login(request,response);

        return Result.success(loginVO);
    }

    @GetMapping("/login/email/code")
    @Operation(summary = "邮箱获取验证码",description = "通过邮箱获取验证码")
    @ApiResponse(responseCode = "200",description = "响应成功，获取验证码成功与否看响应状态码")
    public Result<?> getEmailCode(@Parameter(description = "邮箱") @RequestParam() String email) {

        // 生成验证码并存储redis
        authService.getEmailCode(email);

        return Result.success();
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "注册接口", description = "注册接口")
    @ApiResponse(responseCode = "200",description = "响应成功，注册成功与否看响应状态码")
    public Result<?> register(@RequestBody @Validated RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    @PostMapping("/logout")
    @Operation(summary = "登出接口", description = "登出接口")
    public Result<Void> logout(HttpServletResponse response,
                               @CookieValue(name = "refresh_token",required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new BusinessException(CodeEnum.NO_REFRESH_TOKEN);
        }

        // 构建清除 Cookie（maxAge(0)）
        String clearRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true).secure(true).sameSite("None")
                .maxAge(0).path("/auth").build().toString();
        response.addHeader("Set-Cookie", clearRefresh);

        String username = jwtUtil.extractUsername(refreshToken);

        // 从redis中删除refresh_token
        redisHelper.delete(RedisPrefixConstant.AUTH_REFRESH_TOKEN + username);

        return Result.success();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新接口", description = "使用刷新令牌获取新的token")
    public Result<LoginVO> refresh(@CookieValue(name = "refresh_token",required = false) String refreshToken) {
        log.info("刷新令牌,refreshToken: {}", refreshToken);
        // 刷新令牌不存在
        if (refreshToken == null) {
            return Result.error(CodeEnum.NO_REFRESH_TOKEN);
        }
        // 刷新令牌已过期
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new BusinessException(CodeEnum.TOKEN_EXPIRED);
        }

        // 从redis中获取刷新令牌并验证
        String username = jwtUtil.extractUsername(refreshToken);
        String redisRefreshToken = redisHelper.get(RedisPrefixConstant.AUTH_REFRESH_TOKEN + username,String.class);
        if (!refreshToken.equals(redisRefreshToken)) {
            throw new BusinessException(CodeEnum.TOKEN_EXPIRED);
        }

        // 刷新令牌有效，更新访问令牌
        // 用户信息
        SecurityUser securityUser = (SecurityUser) userService.loadUserByUsername(username);
        User user = securityUser.toUser();

        String accessToken = jwtUtil.generateToken(user.getUsername(), user);
        long expiresIn = jwtUtil.extractExpiresIn(accessToken);

        // 转换为用户视图返回
        UserInfo userInfo = UserInfo.from(user);
        LoginVO loginVO = LoginVO.builder()
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .userInfo(userInfo)
                .build();

        return Result.success(loginVO);
    }
}
