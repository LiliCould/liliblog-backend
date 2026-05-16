package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.constant.LoginStrategyConstant;
import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.request.EmailLoginRequest;
import cn.lilicould.liliblog.pojo.dto.request.PwdLoginRequest;
import cn.lilicould.liliblog.pojo.dto.request.RegisterRequest;
import cn.lilicould.liliblog.pojo.dto.response.LoginVO;
import cn.lilicould.liliblog.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
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
    public Result<Void> logout(HttpServletResponse response) {
        // 构建清除 Cookie（maxAge(0)）
        String clearRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true).secure(true).sameSite("None")
                .maxAge(0).path("/api/auth/refresh").build().toString();
        response.addHeader("Set-Cookie", clearRefresh);

        // todo 从redis中删除refresh_token

        return Result.success();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新接口", description = "使用刷新令牌获取新的token")
    public Result<LoginVO> refresh(@CookieValue(name = "refresh_token",required = false) String refreshToken) {

        if (refreshToken == null) {
            return Result.error(CodeEnum.NO_REFRESH_TOKEN);
        }

        // todo 解析验证刷新令牌，校验redis中是否存在，暂时先打印个日志
        log.info("refreshToken: {}", refreshToken);


        return Result.success();
    }
}
