package com.lilicould.blog.interceptor;

import com.lilicould.blog.exception.BusinessException;
import com.lilicould.blog.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 */

@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 放行OPTIONS请求，允许跨域
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 检查是否是公开接口
        if (isPublicEndpoint(request)) {
            return true;
        }

        // 从请求中获取token
        String token = getTokenFromRequest(request);
        if (token == null) {
            throw new BusinessException("未找到Token信息，请先登录", 401);
        }


        try {
            // 验证token
            if (!jwtUtil.validateToken(token)) {
                throw new BusinessException("Token无效或已过期", 401);
            }

            // 进行到这一步说明Token没有问题，将用户信息存入request
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            request.setAttribute("username", username);
            request.setAttribute("role", role);

            return true;

        } catch (Exception e) {
            throw new BusinessException("Token验证失败: " + e.getMessage(), 401);
        }
    }

    /**
     * 判断是否是公开接口
     * @param request HttpServletRequest对象
     * @return true表示公开接口，false表示非公开接口
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 公开接口定义
        // 登录接口，注册接口，获取公开文章接口，公开文章接口
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                (path.startsWith("/api/articles") && "GET".equalsIgnoreCase(method)) ||
                path.startsWith("/api/public/");
    }

    /**
     * 从请求中获取token
     * @param request HttpServletRequest对象
     * @return token字符串
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 从请求头中获取token，JWT格式为Bearer <token>
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // 去除前缀,返回token
            return bearerToken.substring(7);
        }
        return null;
    }
}
