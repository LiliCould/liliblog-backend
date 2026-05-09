package cn.lilicould.liliblog.pojo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "注册请求参数")
public class RegisterRequest implements Serializable {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,16}$", message = "用户名只能包含字母、数字、下划线，长度为3-16位")
    @Schema(description = "用户名", example = "lilicould", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "lilicould@qq.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 16, message = "昵称长度不能超过16位")
    @Schema(description = "昵称", example = "立里可",requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;

    @Serial
    private final static long serialVersionUID = 1L;
}
