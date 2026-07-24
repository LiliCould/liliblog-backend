package cn.lilicould.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "创建用户参数")
public class UserCreateRequest implements Serializable {

    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String username;

    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    @NotBlank
    private String password;

    @Schema(description = "确认密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String confirmPassword;

    @Schema(description = "邮箱", example = "admin@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String email;

    @Schema(description = "昵称", example = "管理员")
    @Size(max = 16, message = "昵称长度不能超过16位")
    @NotBlank
    private String nickname;
}
