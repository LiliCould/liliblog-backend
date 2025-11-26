package com.lilicould.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    private String token;
    private String username;
    private String nickname;
    private String role;
    private String avatar;
}
