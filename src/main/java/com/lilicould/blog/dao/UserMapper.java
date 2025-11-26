package com.lilicould.blog.dao;

import com.lilicould.blog.entity.User;
import org.apache.ibatis.annotations.Param;


import java.util.Date;

/**
 * 用户数据访问接口
 */
public interface UserMapper {
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return User用户对象
     */
    User selectByUsername(@Param("username") String username);
    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return User用户对象
     */
    User selectByEmail(@Param("email") String email);
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return User用户对象
     */
    User selectById(@Param("id") Long id);
    /**
     * 插入用户
     * @param user 用户对象
     * @return 插入的行数
     */
    int insert(User user);
    /**
     * 更新用户最后登录时间
     * @param id 用户ID
     * @param loginTime 最后登录时间
     * @return 更新的行数
     */
    int updateLastLoginTime(@Param("id") Long id,@Param("loginTime") Date loginTime);
    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 更新的行数
     */
    int update(User user);
    /**
     * 更新用户密码
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 更新的行数
     */
    int updatePassword(@Param("id") Long id,@Param("newPassword") String newPassword);
}
