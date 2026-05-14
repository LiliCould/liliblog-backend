package cn.lilicould.liliblog.pojo.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "邮箱验证码登录请求参数")
public class EmailLoginRequest extends LoginRequest implements Serializable {

    @NotBlank(message = "邮箱不能为空")
    @Schema(description = "邮箱", example = "lilicould@qq.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", example = "655145", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
