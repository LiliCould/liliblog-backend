package cn.lilicould.liliblog.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "管理端用户信息更新参数",description = "管理端用户信息更新参数,对于管理端，修改密码时，只使用密码字段的newPassword，无需旧密码和确认密码")
public class AdminUserUpdateRequest extends UserUpdateRequest implements Serializable {
    @Schema(description = "用户名", example = "admin")
    private String username;
    @Schema(description = "角色，0-管理员，1-普通用户", example = "0")
    private Integer role;
    @Schema(description = "状态，0-禁用，1-启用", example = "1")
    private Integer status;
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;
}
