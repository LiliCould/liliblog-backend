package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.pojo.dto.request.LoginRequest;
import cn.lilicould.liliblog.pojo.dto.request.RegisterRequest;
import cn.lilicould.liliblog.pojo.dto.response.LoginVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

public interface AuthService {

    /**
     * 登录服务接口
     * @param request 登录参数
     * @param response HttpServletResponse,用于设置Cookie
     * @return LoginVO 登录成功响应
     */
    LoginVO login(@Valid LoginRequest request, HttpServletResponse response);

    /**
     * 注册服务接口
     * @param request 注册参数
     */
    void register(@Valid RegisterRequest request);
}
