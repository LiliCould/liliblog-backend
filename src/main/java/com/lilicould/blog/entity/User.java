package com.lilicould.blog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户实体类
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String nickname;
    private String avatar;
    private String role; // ADMIN,VISITOR
    private Integer status;
    private Date lastLoginTime;
    private Date createTime;
    private Date updateTime;

}