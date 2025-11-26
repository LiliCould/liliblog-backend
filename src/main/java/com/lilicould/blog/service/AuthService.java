package com.lilicould.blog.service;

import com.lilicould.blog.dto.LoginDTO;
import com.lilicould.blog.dto.PasswordChangeDTO;
import com.lilicould.blog.dto.RegisterDTO;
import com.lilicould.blog.vo.LoginVO;
import com.lilicould.blog.vo.UserVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    LoginVO login(LoginDTO loginDTO);
    void register(RegisterDTO registerDTO);
    void logout(String token);
    void updatePassword(String username, PasswordChangeDTO passwordChangeDTO);
    UserVO getProfile(String username);
}
