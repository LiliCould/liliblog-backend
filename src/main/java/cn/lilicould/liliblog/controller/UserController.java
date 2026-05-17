package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.context.BaseContext;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.response.UserInfo;
import cn.lilicould.liliblog.pojo.entity.User;
import cn.lilicould.liliblog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户接口")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息",description = "无需管理员权限，无需登录")
    public Result<UserInfo> getUserInfo(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getById(id);

        return Result.success(UserInfo.from(user));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息",description = "无需管理员权限，需要登录")
    public Result<UserInfo> getCurrentUserInfo() {
        User user = userService.getById(BaseContext.getCurrentUserId());

        return Result.success(UserInfo.from(user));
    }
}
