package cn.lilicould.liliblog.pojo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data // 生成getter和setter供springdoc生成文档
@Schema(name = "登录请求参数父类")
public class LoginRequest implements Serializable {

    @Schema(description = "登录类型：pwd/sms/wechat", requiredMode = Schema.RequiredMode.REQUIRED)
    private String loginType;

    @Serial
    private final static long serialVersionUID = 1L;
}
