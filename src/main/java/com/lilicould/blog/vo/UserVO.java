package com.lilicould.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private Date lastLoginTime;
    private Date createTime; // 账号创建时间
}
