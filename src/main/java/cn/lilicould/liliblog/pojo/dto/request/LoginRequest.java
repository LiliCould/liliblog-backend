package cn.lilicould.liliblog.pojo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // 生成getter和setter供springdoc生成文档
@Schema(description = "登录请求参数")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名",example = "lilicould",requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码",example = "123456",requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
