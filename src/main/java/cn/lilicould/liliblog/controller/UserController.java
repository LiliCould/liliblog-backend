package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.context.BaseContext;
import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.request.UserUpdateRequest;
import cn.lilicould.liliblog.pojo.dto.response.UserInfo;
import cn.lilicould.liliblog.pojo.entity.User;
import cn.lilicould.liliblog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户接口")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
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

    @PutMapping
    @Operation(summary = "更新用户信息(/修改密码)",description = "无需管理员权限，需要登录,若为修改密码，成功后前端自主调用登出接口，修改其他内容也要调用/api/user/me接口更新一下信息")
    public Result<?> updateUserInfo(@RequestBody UserUpdateRequest userUpdateRequest) {

        User user = userService.getById(BaseContext.getCurrentUserId());

        if (user == null || StatusConstant.DISABLED.equals(user.getStatus())) { // 如果用户不存在或者被禁用，则返回错误
            throw new BusinessException(CodeEnum.USER_NOT_FOUND);
        }

        // 处理修改密码
        if (userUpdateRequest.getOldPassword() != null) {
            if (!passwordEncoder.matches(userUpdateRequest.getOldPassword(), user.getPassword())) {
                throw new BusinessException(CodeEnum.OLD_PASSWORD_ERROR);
            }
        }

        // 验证两次密码是否匹配
        if (!userUpdateRequest.getNewPassword().equals(userUpdateRequest.getConfirmPassword())) {
            throw new BusinessException(CodeEnum.PASSWORD_NOT_MATCH);
        } else {
            user.setPassword(passwordEncoder.encode(userUpdateRequest.getNewPassword()));
        }

        // 昵称验证
        if (userUpdateRequest.getNickname() != null) {
            user.setNickname(userUpdateRequest.getNickname());
        }

        // 头像验证
        if (userUpdateRequest.getAvatar() != null) {
            user.setAvatar(userUpdateRequest.getAvatar());
        }

        userService.updateById(user);

        return Result.success();
    }
}
