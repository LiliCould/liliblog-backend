package cn.lilicould.liliblog.controller.admin;

import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.query.UserQuery;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.pojo.dto.response.UserInfo;
import cn.lilicould.liliblog.pojo.entity.User;
import cn.lilicould.liliblog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/api/admin/user")
@Tag(name = "用户管理结构", description = "管理员后台，管理用户")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "分页用户列表", description = "分页查询用户列表,所有用户")
    public Result<PageInfo<UserInfo>> list(@ParameterObject UserQuery query) {
        // 设置分页默认值
        if (query.getCurrent() == null) {
            query.setCurrent(1L);
        }
        if (query.getSize() == null) {
            query.setSize(10L);
        }

        PageInfo<UserInfo> pageInfo = userService.list(query);

        return Result.success(pageInfo);
    }

    @GetMapping("/id")
    @Operation(summary = "用户详情管理", description = "根据用户ID查询用户详情")
    public Result<UserInfo> detail(Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(CodeEnum.USER_NOT_FOUND);
        }
        return Result.success(UserInfo.from(user));
    }
}
