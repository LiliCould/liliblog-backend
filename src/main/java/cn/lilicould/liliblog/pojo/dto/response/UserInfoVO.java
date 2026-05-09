package cn.lilicould.liliblog.pojo.dto.response;

import cn.lilicould.liliblog.pojo.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer role;
    private Integer status;

    public static UserInfoVO from(User user) {
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
