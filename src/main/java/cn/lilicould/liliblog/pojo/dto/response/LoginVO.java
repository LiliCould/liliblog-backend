package cn.lilicould.liliblog.pojo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "登录成功结果对象")
public class LoginVO {
    @Schema(description = "访问令牌")
    private String accessToken;    // JWT 访问令牌

    @Schema(description = "过期时间")
    private Long expiresIn;
    @Schema(description = "用户信息")
    private UserInfoVO userInfo;   // 用户基本信息
}
