package cn.lilicould.liliblog.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "用户信息更新参数")
public class UserUpdateRequest implements Serializable {

    @Schema(description = "旧密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oldPassword;

    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    @Schema(description = "新密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @Schema(description = "确认密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String confirmPassword;

    @Size(min = 2,max = 16, message = "昵称长度需要2-16位")
    @Schema(description = "昵称", example = "立里可",requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;

    @Schema(description = "头像,前端处理先调用文件上传接口获得url后直接传入，而不是让用户填写url")
    private String avatar;
}
