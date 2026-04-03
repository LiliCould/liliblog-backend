package com.lilicould.blog.service;

import com.lilicould.blog.dto.LoginDTO;
import com.lilicould.blog.dto.PasswordChangeDTO;
import com.lilicould.blog.dto.RegisterDTO;
import com.lilicould.blog.dto.UserUpdateDTO;
import com.lilicould.blog.vo.LoginVO;
import com.lilicould.blog.vo.UserVO;
import jakarta.validation.Valid;

/**
 * 认证服务接口
 */
public interface AuthService {
    LoginVO login(LoginDTO loginDTO);
    void register(RegisterDTO registerDTO);
    void logout(String token);
    void updatePassword(String username, PasswordChangeDTO passwordChangeDTO);
    UserVO getProfile(String username);

    /**
     * 修改用户信息
     * @param userUpdateDTO 用户信息
     */
    void updateProfile(@Valid UserUpdateDTO userUpdateDTO);

    /**
     * 获取验证码
     * @param email 邮箱
     * @return 验证码
     */
    void getCaptcha(String email);
}
