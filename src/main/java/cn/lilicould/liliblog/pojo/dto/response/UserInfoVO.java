package cn.lilicould.liliblog.pojo.dto.response;

import cn.lilicould.liliblog.pojo.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(name = "用户信息")
public class UserInfoVO implements Serializable {
    @Schema(description = "用户ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "头像URL")
    private String avatar;
    @Schema(description = "角色，0-管理员，1-普通用户")
    private Integer role;
    @Schema(description = "状态，0-禁用，1-启用")
    private Integer status;
    @Schema(description = "上次登录时间",type = "string",format = "date-time",example = "2026-05-09 14:51:06")
    private LocalDateTime lastLoginTime;

    public static UserInfoVO from(User user) {
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }
}
