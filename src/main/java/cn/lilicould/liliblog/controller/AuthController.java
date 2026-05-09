package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.request.LoginRequest;
import cn.lilicould.liliblog.pojo.dto.request.RegisterRequest;
import cn.lilicould.liliblog.pojo.dto.response.LoginVO;
import cn.lilicould.liliblog.service.AuthService;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口")
@ApiSupport(author = "lilicould")
public class AuthController {

    private final AuthService authService;          // 你的用户服务

    @PostMapping("/login")
    @Operation(summary = "登录接口", description = "通过账号密码登录")
    @ApiResponse(responseCode = "200",description = "响应成功，登录成功与否看响应状态码")
    public Result<LoginVO> login(@RequestBody @Valid LoginRequest request,
                                 HttpServletResponse response) {

        LoginVO loginVO = authService.login(request,response);

        return Result.success(loginVO);
    }

    @PostMapping("/register")
    @Operation(summary = "注册接口", description = "注册接口")
    @ApiResponse(responseCode = "200",description = "响应成功，注册成功与否看响应状态码")
    public Result<?> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }
}
