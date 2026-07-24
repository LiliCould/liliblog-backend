package cn.lilicould.liliblog.controller.admin;

import cn.lilicould.enums.CodeEnum;
import cn.lilicould.exception.BusinessException;
import cn.lilicould.result.Result;
import cn.lilicould.liliblog.util.PageUtil;
import cn.lilicould.query.UserQuery;
import cn.lilicould.request.AdminUserUpdateRequest;
import cn.lilicould.request.UserCreateRequest;
import cn.lilicould.response.PageInfo;
import cn.lilicould.response.UserInfo;
import cn.lilicould.entity.User;
import cn.lilicould.liliblog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/api/admin/user")
@Tag(name = "用户管理接口", description = "管理员后台，管理用户")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "分页用户列表", description = "分页查询用户列表,所有用户")
    public Result<PageInfo<UserInfo>> list(@ParameterObject @Validated UserQuery query) {
        // 设置分页默认值
        PageUtil.setDefault(query);

        PageInfo<UserInfo> pageInfo = userService.list(query);

        return Result.success(pageInfo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id获取用户详情", description = "根据用户ID查询用户详情")
    public Result<UserInfo> detail(@Parameter(description = "用户ID", example = "1") @PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(CodeEnum.USER_NOT_FOUND);
        }
        return Result.success(UserInfo.from(user));
    }

    @PutMapping("/id")
    @Operation(summary = "修改用户信息", description = "管理端根据用户ID更新用户详情，修改时若无修改可以为null，但不要传空字符串，建议配合获取用户信息接口使用")
    public Result<?> update(@Parameter(description = "用户ID", example = "1") Long id, @Validated @RequestBody AdminUserUpdateRequest request) {

        userService.updateUserInfo(id, request);

        return Result.success();
    }

    @DeleteMapping("/id")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public Result<?> delete(@Parameter(description = "用户ID", example = "1") Long id) {

        userService.remove(id);
        return Result.success();
    }

    @PostMapping()
    @Operation(summary = "添加用户", description = "添加用户")
    public Result<?> add(@Validated @RequestBody UserCreateRequest request) {

        userService.createUser(request);

        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "切换用户状态", description = "切换用户状态，当然也可使用更新接口，但此接口专门用来切换状态")
    public Result<?> toggleStatus(@Parameter(description = "用户ID", example = "1") @PathVariable Long id){

        userService.changeStatus(id);

        return Result.success();
    }
}
