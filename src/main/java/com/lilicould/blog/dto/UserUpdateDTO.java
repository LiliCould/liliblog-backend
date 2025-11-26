package com.lilicould.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户更新DTO，没有用户创建DTO，因为用户创建操作是通过注册操作实现的
 * @author lilicould
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    private String nickname;
    private String avatar;
    private String email;
}
