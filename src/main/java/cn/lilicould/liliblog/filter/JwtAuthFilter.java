package cn.lilicould.liliblog.filter;

import cn.lilicould.liliblog.config.security.SecurityUser;
import cn.lilicould.liliblog.service.UserService;
import cn.lilicould.liliblog.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 获取请求头中的 Authorization 字段
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // 如果没有 Token 或格式不对，直接放行（让后续逻辑处理未认证的情况）
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 提取 Token（去掉 "Bearer " 前缀）
        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 从 Token 中解析用户名
            String username = jwtUtil.extractUsername(token);

            // 如果当前 SecurityContext 没有认证信息，则进行认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityUser user = (SecurityUser) userService.loadUserByUsername(username);

                // 验证 Token 有效性并设置认证信息
                if (jwtUtil.validateToken(token, username) && user.isEnabled()) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null, // 凭证：传统模式是密码。由于使用jwt，所以这里设置为空
                            user.getAuthorities()
                    );
                    // 将信息存到Security上下文中
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // todo 应该返回前端统一处理
            log.error("JWT 认证失败", e);
        }

        // 继续执行后续过滤器链
        filterChain.doFilter(request, response);
    }
}
