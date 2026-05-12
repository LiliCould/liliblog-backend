package cn.lilicould.liliblog.strategy;

import cn.lilicould.liliblog.pojo.dto.request.LoginRequest;
import cn.lilicould.liliblog.pojo.dto.response.LoginVO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登录策略接口
 */
public interface LoginStrategy {
    /**
     * 实际登录逻辑
     */
    LoginVO login(LoginRequest request, HttpServletResponse response);

    /**
     * 返回当前登录类型标识：pwd / sms / wechat
     */
    String getType();
}
