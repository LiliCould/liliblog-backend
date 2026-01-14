package com.lilicould.blog.controller;

import com.lilicould.blog.dto.LoginDTO;
import com.lilicould.blog.dto.PasswordChangeDTO;
import com.lilicould.blog.dto.RegisterDTO;
import com.lilicould.blog.service.AuthService;
import com.lilicould.blog.vo.LoginVO;
import com.lilicould.blog.vo.ResultVO;
import com.lilicould.blog.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证服务控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    /**
     * 登录
     * @param loginDTO 登录信息
     * @return 登录成功信息
     */
    @PostMapping("/login")
    public ResultVO<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return ResultVO.success(loginVO);
    }

    /**
     * 注册
     * @param registerDTO 注册信息
     * @return 注册成功信息
     */
    @PostMapping("/register")
    public ResultVO<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return ResultVO.success("注册成功");
    }

    /**
     * 修改密码
     * @param passwordChangeDTO 修改密码信息
     * @return 修改成功信息
     */
    @PutMapping("/password")
    public ResultVO<Void> updatePassword(
            @Valid @RequestBody PasswordChangeDTO passwordChangeDTO,
            @RequestAttribute("username") String username // 从request中获取用户名，会自动获取，无需手动输入
    ) {
        authService.updatePassword(username,passwordChangeDTO);
        return ResultVO.success("修改密码成功");
    }

    /**
     * 获取用户个人信息
     */
    @GetMapping("/profile")
    public ResultVO<UserVO> getProfile(@RequestAttribute("username") String username) {
        UserVO userVO = authService.getProfile(username);
        return ResultVO.success("获取用户信息成功", userVO);
    }

    /**
     * 登出
     * @param request 请求对象
     * @return 登出成功信息
     */
    @PostMapping("/logout")
    public ResultVO<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            authService.logout(token);
        }
        return ResultVO.success("退出成功");
    }
}
